package fr.kokyett.rsspire.utils

import android.graphics.Bitmap
import android.text.Html
import android.webkit.URLUtil
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class HtmlUtils {
    companion object {
        val patternOpenGraphIcon = Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternMetaContent = Pattern.compile("content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternIcon = Pattern.compile("<link[^>]*rel=[\"'][^\"']*icon[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternHref = Pattern.compile("href=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternLinks: Pattern = Pattern.compile("(href|src)=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternLinkRss = Pattern.compile("<link[^>]*type=[\"']application/rss\\+xml[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        fun getIcon(url: String): ByteArray? {
            if (!URLUtil.isValidUrl(url))
                return null

            var savedBitmap: Bitmap? = null
            try {
                var content = DownloaderUtils.getString(url) ?: ""
                content = restoreLinks(url, content)

                var matcher = patternOpenGraphIcon.matcher(content)
                if (matcher.find()) {
                    val matcherContent = matcher.group(0) ?: ""
                    matcher = patternMetaContent.matcher(matcherContent)
                    if (matcher.find()) {
                        val bitmapUrl = matcher.group(1) ?: ""
                        if (URLUtil.isValidUrl(bitmapUrl)) {
                            val bitmap = DownloaderUtils.getBitmap(bitmapUrl)
                            if (bitmap != null)
                                savedBitmap = bitmap
                        }
                    }
                }

                matcher = patternIcon.matcher(content)
                while (matcher.find()) {
                    val matcherContent = matcher.group(0) ?: continue
                    val hrefMatcher: Matcher = patternHref.matcher(matcherContent)
                    if (hrefMatcher.find()) {
                        val bitmapUrl = hrefMatcher.group(1) ?: continue
                        if (URLUtil.isValidUrl(bitmapUrl)) {
                            val bitmap = DownloaderUtils.getBitmap(bitmapUrl)
                            if (bitmap != null && (savedBitmap == null || savedBitmap.width < bitmap.width))
                                savedBitmap = bitmap
                        }
                    }
                }

                if (savedBitmap == null) {
                    try {
                        var iconUrl = URL(url)
                        iconUrl = URL(iconUrl.protocol + "://" + iconUrl.host + "/favicon.png")

                        var bitmap = DownloaderUtils.getBitmap(iconUrl)
                        if (bitmap == null) {
                            iconUrl = URL(iconUrl.protocol + "://" + iconUrl.host + "/favicon.ico")
                            bitmap = DownloaderUtils.getBitmap(iconUrl)
                        }
                        savedBitmap = bitmap
                    } catch (e: Exception) {
                        //TODO: Log exception
                    }
                }
            } catch (e: Exception) {
                //TODO: Log exception
            }

            if (savedBitmap != null) {
                val stream = ByteArrayOutputStream()
                savedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()
                stream.close()
                return byteArray
            }
            return null
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
                            .startsWith("https://") || link.lowercase(Locale.getDefault())
                            .startsWith("data:")
                    ) continue

                    var newLink: String
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
                    newContent =
                        newContent.replace(matchContent, matchContent.replace(link, newLink))
                }
                return newContent
            } catch (e: java.lang.Exception) {
                //TODO Log exception
                return content
            }
        }
    }
}