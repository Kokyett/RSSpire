package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.kokyett.rsspire.RSSpireApplication
import fr.kokyett.rsspire.database.entities.Category
import fr.kokyett.rsspire.database.entities.Feed
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class ImportWorker(context: Context, private var params: WorkerParameters) : CoroutineWorker(context, params) {
    private var categoryRepository = (applicationContext as RSSpireApplication).categoryRepository
    private var feedRepository = (applicationContext as RSSpireApplication).feedRepository

    override suspend fun doWork(): Result {
        return try {
            applicationContext.contentResolver.openInputStream(Uri.parse(params.inputData.getString("URI"))).use {
                inputStream ->
                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                readXml(builder.parse(InputSource(inputStream)))
            }
            Result.success()
        } catch (e: Exception) {
            RSSpireApplication.logException(e)
            Result.failure()
        }
    }

    private suspend fun readXml(document: Document) {
        val root = document.documentElement
        if (root.nodeName.lowercase() != "opml") {
            RSSpireApplication.logInformation("ImportWorker.readXml: not an OPML file")
            return
        }

        val nodes = root.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeName.lowercase() == "body") {
                readOutlines(node.childNodes)
            } else {
                RSSpireApplication.logInformation("ImportWorker.readXml: unknown node ${node.nodeName}")
            }
        }
    }

    private suspend fun readOutlines(nodes: NodeList, category: Category? = null) {
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeName.lowercase() != "outline") {
                RSSpireApplication.logInformation("ImportWorker.readXml: unknown node ${node.nodeName}")
                continue
            }

            var title = node.attributes.getNamedItem("title")?.nodeValue ?: node.attributes.getNamedItem("text")?.nodeValue
            if (title != null && title.trim() == "")
                title = null;

            val url = node.attributes.getNamedItem("xmlUrl")?.nodeValue ?: ""
            if (title != null && url.trim() == "") {
                readOutlines(node.childNodes, categoryRepository.get(title))
                continue
            }

            if (URLUtil.isValidUrl(url)) {
                var feed = feedRepository.get(url)
                if (feed == null) {
                    feed = Feed(0, category?.id, url, title)
                    feedRepository.save(feed)
                }
            } else {
                RSSpireApplication.logInformation("ImportWorker.readOutlines: Invalid url for $url")
            }
        }
    }
}