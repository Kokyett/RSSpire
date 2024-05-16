package fr.kokyett.rsspire.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.URLUtil
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.enums.LogType
import fr.kokyett.rsspire.models.FeedIcon
import fr.kokyett.rsspire.utils.DateTime
import fr.kokyett.rsspire.utils.Downloader
import fr.kokyett.rsspire.utils.Html
import fr.kokyett.rsspire.utils.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

class RefreshFeedsWorker(context: Context, private var params: WorkerParameters) : CoroutineWorker(context, params) {
    private lateinit var log: Log
    private var feedRepository = ApplicationContext.getFeedRepository()
    private var entryRepository = ApplicationContext.getEntryRepository()

    private val dateReplaces = arrayOf(
        arrayOf("Z", "GMT"), arrayOf("MEST", "+0200"), arrayOf("EDT", "-0400"), arrayOf("EST", "-0500"), arrayOf("PST", "-0800")
    )
    private val dateFormats: Array<SimpleDateFormat> = arrayOf(
        SimpleDateFormat("yy-MM-dd'T'HH:mm:ss.SSSz", Locale.getDefault()),
        SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()),
        SimpleDateFormat("yy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss' 'Z", Locale.getDefault()),
        SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss' 'Z", Locale.ENGLISH),
        SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm' 'Z", Locale.getDefault()),
        SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm' 'Z", Locale.ENGLISH),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yy' 'HH:mm:ss' 'z", Locale.getDefault()),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yy' 'HH:mm:ss' 'z", Locale.ENGLISH),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yy' 'HH:mm' 'z", Locale.getDefault()),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yy' 'HH:mm' 'z", Locale.ENGLISH),

        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("d' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.getDefault()),
        SimpleDateFormat("d' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.ENGLISH),
        SimpleDateFormat("d' 'MMM' 'yyyy' 'HH:mm' 'Z", Locale.getDefault()),
        SimpleDateFormat("d' 'MMM' 'yyyy' 'HH:mm' 'Z", Locale.ENGLISH),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yyyy' 'HH:mm:ss' 'z", Locale.getDefault()),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yyyy' 'HH:mm:ss' 'z", Locale.ENGLISH),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yyyy' 'HH:mm' 'z", Locale.getDefault()),
        SimpleDateFormat("EEE', 'd' 'MMM' 'yyyy' 'HH:mm' 'z", Locale.ENGLISH),
        SimpleDateFormat("MMM' 'dd', 'yyyy", Locale.getDefault()),
        SimpleDateFormat("MMM' 'dd', 'yyyy", Locale.ENGLISH),
    )

    override suspend fun doWork(): Result {
        val startDate = System.currentTimeMillis()
        log = Log(LogType.UPDATEFEEDS)
        return try {
            log.writeInformation("Delete entries")
            ApplicationContext.getEntryRepository().deleteReadEntries()
            ApplicationContext.getRawRepository().vacuum()
            log.writeInformation("Start refresh feeds")

            val idFeed = params.inputData.getLong("ID_FEED", 0)
            log.writeInformation("Refresh with feed id: $idFeed")
            val feedParam = feedRepository.get(idFeed)
            val list = if (feedParam != null) {
                listOf(feedParam)
            } else {
                feedRepository.getRefresh()
            }

            for (feed in list) {
                if (System.currentTimeMillis() - startDate > 10 * DateTime.MINUTE) {
                    log.writeInformation("Stopping refresh: 10 minutes have passed")
                    break
                }

                log.writeInformation("--> ${feed.url}")

                try {
                    if (feed.icon == null && feed.iconUrl != null) {
                        try {
                            feed.icon = Downloader.getBytes(URL(feed.iconUrl))
                            ApplicationContext.getFeedRepository().save(feed)
                        } catch (_: Exception) {

                        }
                    }

                    if (!URLUtil.isValidUrl(feed.url))
                        continue

                    var content = Downloader.getString(feed.url) ?: ""
                    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    val document = withContext(Dispatchers.IO) {
                        try {
                            builder.parse(InputSource(StringReader(content)))
                        } catch (e1: java.lang.Exception) {
                            try {
                                builder.parse(feed.url)
                            } catch (e2: java.lang.Exception) {
                                val pattern: Pattern = Pattern.compile("&(?!(?:#\\d+|#x[0-9a-f]+|\\w+);)")
                                val matcher: Matcher = pattern.matcher(content)
                                while (matcher.find()) {
                                    content = content.replace(matcher.group(0) ?: "", "&amp;")
                                }
                                builder.parse(InputSource(StringReader(content)))
                            }
                        }
                    }
                    readXml(document, feed)
                    feed.nextRefreshDate = Date(System.currentTimeMillis() + feed.refreshInterval)
                    feedRepository.save(feed)
                } catch (e: Exception) {
                    log.writeException(e)
                }
            }
            Result.success()
        } catch (e: Exception) {
            log.writeException(e)
            Result.failure()
        } finally {
            log.writeInformation("End refresh feeds")
            log.save()
        }
    }

    private fun readXml(document: Document, feed: Feed) {
        val root = document.documentElement
        when (root.nodeName.lowercase()) {
            "rss" -> readRss(root.childNodes, feed)
            "feed", "rdf:rdf" -> readFeed(root.childNodes, feed)
            else -> log.writeInformation("readXml -> Unknown node: " + root.nodeName)
        }
    }

    private fun doNothing() {
        // Do nothing to avoid known nodes logs
    }

    private fun readRss(nodes: NodeList, feed: Feed) {
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            when (node.nodeName.lowercase()) {
                "#text" -> {}
                "channel" -> readFeed(node.childNodes, feed)
                else -> log.writeInformation("readRss -> Unknown node: " + node.nodeName)
            }
        }
    }

    private fun readFeed(nodes: NodeList, feed: Feed) {
        val previousLastEntryDate: Date? = feed.lastEntryDate
        val icons = ArrayList<FeedIcon>()
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            when (node.nodeName.lowercase()) {
                "title", "dc:title" -> feed.title = feed.title ?: node.textContent.trim()
                "description", "subtitle" -> feed.description = feed.description ?: node.textContent.trim()
                "image", "icon", "logo", "itunes:image" -> readIcon(node, feed)?.let { icons.add(it) }
                "entry", "item" -> readEntry(node.childNodes, feed, previousLastEntryDate)

                "#comment", "id", "atom:id", "link", "atom:link", "language", "dc:language",
                "opensearch:totalresults", "opensearch:startindex", "opensearch:itemsperpage",
                "dc:date", "pubdate", "updated",
                "author", "dc:creator", "managingeditor", "webmaster",
                "dc:rights",
                "admin:generatoragent", "admin:errorreportsto", "ttl", "xhtml:meta",
                "blogchannel:blogroll", "blogchannel:blink", "geo:lat", "geo:long", "textinput",
                "docs", "category", "cloud", "lastbuilddate", "copyright", "site", "generator",
                "sy:updatebase", "sy:updateperiod", "sy:updatefrequency" -> doNothing()

                else ->  if (!node.nodeName.lowercase().startsWith("itunes:")) logNode(node, "Feed ")
            }

            var biggestBitmap: Bitmap? = null
            for (icon in icons) {
                val bitmap = BitmapFactory.decodeByteArray(icon.byteArray, 0, icon.byteArray!!.size)
                if (biggestBitmap == null || biggestBitmap.width * biggestBitmap.height < bitmap.width * bitmap.height) {
                    biggestBitmap = bitmap
                }
            }
            if (biggestBitmap != null) {
                val stream = ByteArrayOutputStream()
                biggestBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                feed.icon = stream.toByteArray()
                stream.close()
            }
        }
    }

    private fun readIcon(node: Node, feed: Feed): FeedIcon? {
        if (feed.icon != null || feed.iconUrl != null) return null

        var url = node.textContent.trim()
        for (i in 0 until node.attributes.length) {
            val attribute = node.attributes.item(i)
            when (attribute.nodeName.lowercase()) {
                "url", "href" -> url = attribute.textContent.trim()
            }
        }
        for (i in 0 until node.childNodes.length) {
            val childNode = node.childNodes.item(i)
            when (childNode.nodeName.lowercase()) {
                "url" -> url = childNode.textContent.trim()
            }
        }
        return try {
            val bytes = Downloader.getBytes(URL(url))
            FeedIcon(url, bytes)
        } catch (e: Exception) {
            log.writeException("Feed icon URL: $url")
            log.writeException(e)
            null
        }
    }

    private fun readEntry(nodes: NodeList, feed: Feed, previousLastEntryDate: Date?) {
        val imageList = ArrayList<String>()
        val entry = Entry(idFeed = feed.id)
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            when (node.nodeName.lowercase()) {
                "id", "guid", "dc:identifier" -> readGuid(node, entry)
                "link", "atom:link" -> readLink(node, feed, entry)
                "title", "itunes:title"-> entry.title = node.textContent.trim()
                "content", "content:encoded", "description" -> readContent(node, entry)
                "summary", "atom:summary", "itunes:summary" -> readContent(node, entry)
                "category", "betag:tag", "itunes:keywords", "media:keywords" -> readCategory(node, entry)
                "dcterms:created", "dcterms:modified" -> readDate(node, feed, entry)
                "pubdate", "published", "dc:date", "dc:modified", "dc:created" -> readDate(node, feed, entry)
                "updated", "atom:updated" -> readDate(node, feed, entry)
                "author", "dc:creator", "dc:publisher" -> readAuthor(node, entry)
                "enclosure", "media:thumbnail", "media:content", "itunes:image", "image" -> readEnclosure(node, imageList)

                "#cdata-section",
                "dc:format", "dc:language", "source", "thr:total",
                "pingback:server", "pingback:target", "trackback:ping", "wfw:comment",
                "rssplus:free", "comments", "wfw:commentrss", "slash:comments", "language", "post-id" -> doNothing()
                else -> if (!node.nodeName.lowercase().startsWith("itunes:")) logNode(node, "Entry ")
            }
        }

        if (entry.guid == null) entry.guid = entry.link
        if (entry.link != null) Html.restoreLinks(entry.link!!, entry.content ?: "")
        if (entry.title == null) entry.title = entry.link

        val existingEntry = ApplicationContext.getEntryRepository().getExisting(entry.idFeed, entry.guid)
        if (existingEntry?.publishDate != null) {
            if (entry.publishDate == null)
                return
            if (entry.publishDate!! <= existingEntry.publishDate)
                return
        }

        if (entry.publishDate == null)
            entry.publishDate = Date()

        if ((feed.lastEntryDate == null || entry.publishDate!! > feed.lastEntryDate) && entry.publishDate!! <= Date())
            feed.lastEntryDate = entry.publishDate

        if (previousLastEntryDate != null && entry.publishDate!! <= previousLastEntryDate)
            return

        if (feed.downloadFullContent) {
            try {
                val content = Downloader.getString(entry.link!!)
                if (content != null && content.lowercase() != "") {
                    entry.content = Html.restoreLinks(entry.link!!, content)
                    entry.content = Html.formatFullContent(entry.content!!)
                }
            } catch (_: Exception) {

            }
        }
        if (feed.replaceThumbnails) {
            entry.content = Html.replaceThumbnails(entry.content!!)
        }
        entry.content = Html.cleanUpContent(entry.content!!)

        entry.content?.let { entry.content = Html.restoreLinks(entry.link!!, it) }

        try {
            entryRepository.save(entry)
        } catch (e: Exception) {
            log.writeException(e)
        }
    }

    private fun readEnclosure(node: Node, imageList: ArrayList<String>) {
        var url = ""
        var type = ""
        try {
            for (i in 0 until node.attributes.length) {
                when (node.attributes.item(i).nodeName.lowercase()) {
                    "url", "href" -> url = node.attributes.item(i).textContent.trim()
                    "type" -> type = node.attributes.item(i).textContent.trim()
                }
            }

            if (url == "") {
                for (i in 0 until node.childNodes.length) {
                    val child = node.childNodes.item(i)
                    when (child.nodeName.lowercase()) {
                        "url" -> url = child.textContent.trim()
                        "type" -> type = node.attributes.item(i).textContent.trim()
                        "enclosure" -> readEnclosure(child, imageList)
                        else -> logNode(child, "Enclosure ")
                    }
                }
            }

            if (type == "" || type.contains("image")) {
                if (url != "" && !imageList.contains(url))
                    imageList.add(url)
            }
        } catch (e: Exception) {
            logNode(node, "ReadEnclosure ")
            log.writeException(e)
        }
    }

    private fun readContent(node: Node, entry: Entry) {
        entry.content =
            if (entry.content == null || (entry.content?.length ?: 0) < node.textContent.trim().length)
                node.textContent.trim()
            else
                entry.content
    }

    private fun readGuid(node: Node, entry: Entry) {
        entry.guid = node.textContent.trim()
        try {
            for (i in 0 until node.attributes.length) {
                if (node.attributes.item(i).nodeName.lowercase() == "ispermalink" && node.attributes.item(i).textContent.lowercase().trim() == "true") {
                    if (entry.link == null && URLUtil.isValidUrl(entry.guid)) {
                        entry.link = entry.guid
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun readLink(node: Node, feed: Feed, entry: Entry) {
        if (entry.link != null) {
            return
        }

        var link = node.textContent.trim()
        if (link == "") {
            var type = ""
            var rel = ""
            try {
                for (i in 0 until node.attributes.length) {
                    when (node.attributes.item(i).nodeName.lowercase()) {
                        "url", "href" -> link = node.attributes.item(i).textContent.trim()
                        "type" -> type = node.attributes.item(i).textContent.lowercase().trim()
                        "rel" -> rel = node.attributes.item(i).textContent.lowercase().trim()
                    }
                }
                if (type != "" && (type != "text/html" || rel != "alternate"))
                    link = ""
            } catch (e: Exception) {
                // Do nothing
            }
        }

        if (link != "") {
            entry.link = Html.restoreLink(URL(feed.url), link)
        }
    }

    private fun readDate(feed: Feed, node: Node): Date? {
        var replacedDate = node.textContent.trim()
        for (rep in dateReplaces) {
            replacedDate = replacedDate.replace(rep[0], rep[1])
        }
        for (df in dateFormats) {
            try {
                return df.parse(replacedDate)
            } catch (p: ParseException) {
                // Do nothing
            }
        }
        val logDate = Log(LogType.PARSEDATE)
        logDate.writeInformation("Failed to parse date ${node.textContent.trim()} for ${feed.url}")
        logDate.save()
        return null
    }

    private fun readDate(node: Node, feed: Feed, entry: Entry) {
        val date = readDate(feed, node)
        if (date == null) {
            logNode(node, "Date ")
            return
        }
        if (entry.publishDate == null || entry.publishDate!! < date) entry.publishDate = date
    }

    private fun readAuthor(node: Node, entry: Entry) {
        if (entry.author != null) return

        entry.author = node.textContent.trim()
        for (i in 0 until node.childNodes.length) {
            if (node.childNodes.item(i).nodeName.lowercase() == "name") {
                entry.author = node.textContent.trim()
            }
        }
        if (entry.author == "") entry.author = null
    }

    private fun readCategory(node: Node, entry: Entry) {
        var category = node.textContent.trim()
        if (category == "") {
            try {
                for (i in 0 until node.attributes.length) {
                    if (node.attributes.item(i).nodeName.lowercase() == "term") {
                        category = node.attributes.item(i).nodeName.lowercase()
                    }
                }
            } catch (e: Exception) {
                // Do nothing
            }
        }
        if (category != "") entry.categories = (if (entry.categories != null) ", " else "") + category
    }

    private fun logNode(node: Node, indent: String) {
        if (node.nodeName == "#text") return

        log.writeInformation(indent + "node " + node.nodeName + ": " + node.textContent.trim())
        val newIndent = "$indent    "
        try {
            for (i in 0 until node.attributes.length) {
                log.writeInformation(newIndent + "attr " + node.attributes.item(i).nodeName + ": " + node.attributes.item(i).textContent.trim())
            }
        } catch (_: Exception) {
        }

        try {
            for (i in 0 until node.childNodes.length) {
                logNode(node.childNodes.item(i), newIndent)
            }
        } catch (_: Exception) {
        }
    }
}
