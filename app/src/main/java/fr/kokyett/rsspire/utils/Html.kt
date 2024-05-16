package fr.kokyett.rsspire.utils

import android.webkit.URLUtil
import fr.kokyett.rsspire.models.FeedIcon
import fr.kokyett.rsspire.models.SearchFeedResult
import java.net.URL
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class Html {
    companion object {
        private val patternHref = Pattern.compile("href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLinks = Pattern.compile("(href|src)=(\"([^\"]*)\"|'([^']*)')", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLinkRss = Pattern.compile("<link[^>]*type=\"application/rss\\+xml\"[^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternOpenGraphIcon = Pattern.compile("<meta[^>]*property=\"og:image\"[^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternMetaContent = Pattern.compile("content=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternIcon = Pattern.compile("<link[^>]*rel=\"[^\"]*icon\"[^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternImage = Pattern.compile("<img[^>]*src=\"([^\"]*)\"[^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLogoImage = Pattern.compile("<img[^>]*src=\"([^\"]*logo[^\"]*)\"[^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        private val patternBody = Pattern.compile("<body[^>]*>(.*)</body>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternArticle = Pattern.compile("<article[^>]*>((?!</article>).)*</article>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternTagToDelete = Pattern.compile("<(aside|footer|header|nav|noscript|script|style)[^>]*>((?!<(/\\1|\\1[^>]*)>).)*</\\1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternTagCLassToDelete = Pattern.compile("<(section|div|ul|ins)[^>]*(class|id)=\"[^\"]*(header|footer|menu|nav|adsbygoogle|comment|related)[^\"]*\"[^>]*>((?!</\\1>).)*</\\1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternTag = Pattern.compile("<([a-z]*)[^>]*>((?!<(/\\1|\\1[^>]*)>).)*</\\1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternComment = Pattern.compile("<!--((?!-->).)*-->", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternEmptyTag = Pattern.compile("<(div)[^>]*>\\s*</\\1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternThumbLink = Pattern.compile("<a\\s[^>]*href=\"([^\"]*)\"[^>]*>\\s*<img\\s[^>]*src=\"([^\"]*)\"[^>]*>\\s*</a>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        fun getRssLinks(content: String): ArrayList<SearchFeedResult> {
            val list = ArrayList<SearchFeedResult>()
            val matcher = patternLinkRss.matcher(content)
            while (matcher.find()) {
                val hrefMatcher: Matcher = patternHref.matcher(matcher.group(0) ?: "")
                if (hrefMatcher.find()) {
                    val searchFeedResult = SearchFeedResult(hrefMatcher.group(1) ?: "")
                    list.add(searchFeedResult)
                }
            }
            return list
        }

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
                    val link = matcher.group(3) ?: (matcher.group(4) ?: continue)

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

        fun replaceThumbnails(initialContent: String): String? {
            var content: String = initialContent
            if (content.trim() == "") {
                return null
            }
            val matcher = patternThumbLink.matcher(content)
            while (matcher.find()) {
                val href = matcher.group(1)!!.lowercase()
                val src = matcher.group(2)!!.lowercase()
                    .replace("thumb/", "")
                    .replace("thumbs/", "")
                    .replace("-thumb", "")
                    .replace("_thumb", "")
                if (href == src)
                    content = content.replace(matcher.group(0)!!, "<img src=\"${matcher.group(1)}\">")
            }
            return content
        }

        fun cleanUpContent(initialContent: String): String? {
            var content: String = initialContent
            if (content.trim() == "") {
                return null
            }

            try {
                var matcher = patternComment.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let { content = content.replace(it, "") }
                }

                matcher = patternImage.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let { content = content.replace(it, "<img src=\"" + matcher.group(1) + "\">") }
                }

                matcher = patternEmptyTag.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let { content = content.replace(it, "") }
                    matcher = patternEmptyTag.matcher(content)
                }
            } catch (_: Exception) {

            }
            return content.ifEmpty { null }
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

                matcher = patternTagCLassToDelete.matcher(content)
                while (matcher.find()) {
                    matcher.group(0)?.let {
                        val matcher2 = patternTag.matcher(it)
                        if (matcher2.find())
                            matcher2.group(0)?.let {it2 ->
                                content = content.replace(it2, "")
                            }
                    }
                    matcher = patternTagCLassToDelete.matcher(content)
                }

                val newContent = StringBuilder()
                matcher = patternArticle.matcher(content)
                while (matcher.find()) {
                    newContent.append(matcher.group(0))
                }
                if (newContent.toString().trim().isNotEmpty()) {
                    content = newContent.toString()
                }

            } catch (_: Exception) {

            }
            return content.ifEmpty { null }
        }
    }
}
