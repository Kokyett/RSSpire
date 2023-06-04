package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import android.util.Log
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
            // TODO: Log error
            Log.e("ImportOPML", "Crash " + e.message, e)
            Result.failure()
        }
    }

    private suspend fun readXml(document: Document) {
        val root = document.documentElement
        if (root.nodeName.lowercase() != "opml") {
            // TODO: Log error
            return
        }

        val nodes = root.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeName.lowercase() == "body") {
                readOutlines(node.childNodes)
            } else {
                // TODO: Log unknown Tag
            }
        }
    }

    private suspend fun readOutlines(nodes: NodeList, category: Category? = null) {
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeName.lowercase() != "outline") {
                // TODO: Log unknown Tag
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

            if (isUrl(url)) {
                var feed = feedRepository.get(url)
                if (feed == null) {
                    feed = Feed(0, category?.id, url, title)
                    feedRepository.save(feed)
                }
            } else {
                // TODO: Log error
            }
        }
    }

    private fun isUrl(url: String) : Boolean {
        if (url.trim() == "") {
            return false
        }
        return try {
            URL(url)
            true
        } catch (e: Exception) {
            false
        }
    }
}