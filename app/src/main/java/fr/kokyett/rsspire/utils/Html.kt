package fr.kokyett.rsspire.utils

import android.webkit.URLUtil
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.models.FeedIcon
import java.net.URL
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


class Html {
    companion object {
        val patternHref: Pattern = Pattern.compile("href=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLinks: Pattern = Pattern.compile("(href|src)=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternLinkRss: Pattern = Pattern.compile("<link[^>]*type=[\"']application/rss\\+xml[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternOpenGraphIcon: Pattern = Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternMetaContent: Pattern = Pattern.compile("content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternIcon: Pattern = Pattern.compile("<link[^>]*rel=[\"'][^\"']*icon[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternImage: Pattern = Pattern.compile("<img[^>]*src=[\"']([^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLogoImage: Pattern = Pattern.compile("<img[^>]*src=[\"']([^\"']*logo[^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        private val patternBody = Pattern.compile("<body[^>]*>(.*)</body>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternArticle = Pattern.compile("<article[^>]*>((?!</article>).)*</article>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLazyImg = Pattern.compile("<img[^>]*(data-src|data-lazy-src)=[\"']([^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternTagToDelete = Pattern.compile("<(aside|footer|header|nav|noscript|script|style)[^>]*>((?!<(/\\1|\\1[^>]*)>).)*</\\1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternStyleAttribute = Pattern.compile("style=[\"'][^\"']*[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        fun restoreLink(url: URL, link: String): String {
            if (URLUtil.isValidUrl(link))
                return link

            val newLink: String
            if (link.startsWith("//")) {
                newLink = url.protocol + ":" + link
            } else if (link.startsWith("/")) {
                newLink = url.protocol + "://" + url.host + link
            } else {
                var pos = url.path.lastIndexOf("/") + 1
                if (pos <= 0) pos = url.path.length
                newLink = url.protocol + "://" + url.host + "/" + if (pos <= 0) {
                    link
                } else {
                    url.path.substring(0, pos) + link
                }
            }
            return newLink
        }

        fun restoreLinks(urlString: String, content: String): String {
            try {
                val list: MutableList<String> = ArrayList()
                val url = URL(urlString)
                var newContent = content
                val matcher: Matcher = patternLinks.matcher(newContent)
                while (matcher.find()) {
                    val matchContent = matcher.group(0) ?: continue
                    val link = matcher.group(2) ?: continue

                    if (list.contains(matchContent)) continue

                    list.add(matchContent)
                    if (link.trim() == "" || link == "/" || link.lowercase(Locale.getDefault())
                            .startsWith("http://") || link.lowercase(Locale.getDefault())
                            .startsWith("https://") || link.lowercase(Locale.getDefault()).startsWith("data:")
                    ) continue

                    val newLink = restoreLink(url, link)
                    newContent = newContent.replace(matchContent, matchContent.replace(link, newLink))
                }
                return newContent
            } catch (e: java.lang.Exception) {
                return content
            }
        }

        fun getImages(content: String): ArrayList<String> {
            val images = ArrayList<String>()
            val matcher = patternImage.matcher(content)
            while (matcher.find()) {
                val matchContent = matcher.group(1) ?: continue
                if (!images.contains(matchContent))
                    images.add(matchContent)
            }
            return images
        }

        fun getIcons(url: String): ArrayList<FeedIcon> {
            val list = ArrayList<FeedIcon>()

            if (!URLUtil.isValidUrl(url)) return list
            try {
                var content = Downloader.getString(url) ?: ""
                content = restoreLinks(url, content)

                var matcher = patternOpenGraphIcon.matcher(content)
                while (matcher.find()) {
                    val matcherContent = matcher.group(0) ?: ""
                    matcher = patternMetaContent.matcher(matcherContent)
                    if (matcher.find()) {
                        val bitmapUrl = matcher.group(1) ?: ""
                        if (URLUtil.isValidUrl(bitmapUrl) && list.none { it.url == bitmapUrl }) {
                            list.add(FeedIcon(bitmapUrl))
                        }
                    }
                }

                matcher = patternIcon.matcher(content)
                while (matcher.find()) {
                    val matcherContent = matcher.group(0) ?: continue
                    val hrefMatcher: Matcher = patternHref.matcher(matcherContent)
                    if (hrefMatcher.find()) {
                        val bitmapUrl = hrefMatcher.group(1) ?: continue
                        if (URLUtil.isValidUrl(bitmapUrl) && list.none { it.url == bitmapUrl }) {
                            list.add(FeedIcon(bitmapUrl))
                        }
                    }
                }

                matcher = patternLogoImage.matcher(content)
                while (matcher.find()) {
                    val bitmapUrl = matcher.group(1) ?: continue
                    if (URLUtil.isValidUrl(bitmapUrl) && list.none { it.url == bitmapUrl }) {
                        list.add(FeedIcon(bitmapUrl))
                    }
                }
            } catch (e: Exception) {
                // TODO LOGS
            }

            try {
                var iconUrl = URL(url)
                iconUrl = URL(iconUrl.protocol + "://" + iconUrl.host + "/favicon.png")
                if (list.none { it.url == iconUrl.toString() }) list.add(FeedIcon(iconUrl.toString()))

                iconUrl = URL(iconUrl.protocol + "://" + iconUrl.host + "/favicon.ico")
                if (list.none { it.url == iconUrl.toString() }) list.add(FeedIcon(iconUrl.toString()))
            } catch (e: Exception) {
                // TODO LOGS
            }
            return list
        }

        fun formatFullContent(initialContent: String): String? {
            var content: String = initialContent
            if (content.trim() == "") {
                return null
            }

            try {
                var matcher = patternBody.matcher(content)
                if (matcher.find()) {
                    content = matcher.group(1) ?: ""
                }

                matcher = patternTagToDelete.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let { content = content.replace(it, "") }
                }

                matcher = patternLazyImg.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let { content = content.replace(it, "<img src=\"" + matcher.group(2) + "\">") }
                }

                val newContent = StringBuilder()
                matcher = patternArticle.matcher(content)
                while (matcher.find()) {
                    newContent.append(matcher.group(0))
                }
                if (newContent.toString().trim().isNotEmpty()) {
                    content = newContent.toString()
                }

                matcher = patternStyleAttribute.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let { content = content.replace(it, "") }
                }
            } catch (_: Exception) {

            }
            return content.ifEmpty { null }
        }
    }
}