package fr.kokyett.rsspire.utils

import java.net.URL
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class Html {
    companion object {
        val patternHref: Pattern = Pattern.compile("href=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val patternLinks: Pattern = Pattern.compile("(href|src)=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternLinkRss: Pattern = Pattern.compile("<link[^>]*type=[\"']application/rss\\+xml[\"'][^>]*>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        fun restoreLink(url: URL, link: String): String {
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
                    if (link.trim() == "" || link == "/" || link.lowercase(Locale.getDefault()).startsWith("http://") || link.lowercase(Locale.getDefault())
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
    }
}