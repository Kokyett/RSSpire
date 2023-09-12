package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.database.entities.Category
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.enums.LogType
import fr.kokyett.rsspire.utils.Log
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory

class ImportWorker(context: Context, private var params: WorkerParameters) : CoroutineWorker(context, params) {
    private lateinit var log: Log

    override suspend fun doWork(): Result {
        log = Log(LogType.IMPORTOPML)
        return try {
            applicationContext.contentResolver.openInputStream(Uri.parse(params.inputData.getString("URI"))).use { inputStream ->
                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                readXml(builder.parse(InputSource(inputStream)))
            }
            log.save()
            Result.success()
        } catch (e: Exception) {
            log.writeException("Error on exporting OPML file")
            log.writeException(e)
            log.save()
            Result.failure()
        }
    }

    private fun readXml(document: Document) {
        val root = document.documentElement
        if (root.nodeName.lowercase() != "opml") {
            log.writeInformation("Not an OPML file")
            return
        }

        val nodes = root.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeName.lowercase() == "body") {
                readOutlines(node.childNodes)
            }
        }
    }

    private fun readOutlines(nodes: NodeList, category: Category? = null) {
        val feedRepository = ApplicationContext.getFeedRepository()
        val categoryRepository = ApplicationContext.getCategoryRepository()

        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeName.lowercase() != "outline") {
                continue
            }

            var title = node.attributes.getNamedItem("title")?.nodeValue ?: node.attributes.getNamedItem("text")?.nodeValue
            if (title != null && title.trim() == "")
                title = null

            val url = node.attributes.getNamedItem("xmlUrl")?.nodeValue ?: ""
            if (title != null && url.trim() == "") {
                log.writeInformation("No url: create category $title")
                readOutlines(node.childNodes, categoryRepository.get(title))
                continue
            }

            if (URLUtil.isValidUrl(url)) {
                log.writeInformation("Saving $url")
                var feed = feedRepository.getForUrl(url)
                if (feed == null) {
                    feed = Feed(
                        idCategory = category?.id,
                        url = url,
                        title = title
                    )
                    feedRepository.save(feed)
                }
            } else {
                log.writeInformation("Invalid url for $url")
            }
        }
    }
}