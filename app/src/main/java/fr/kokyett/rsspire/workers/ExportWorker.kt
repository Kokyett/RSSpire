package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import androidx.lifecycle.asFlow
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.enums.LogType
import fr.kokyett.rsspire.utils.DateTime
import fr.kokyett.rsspire.utils.Log
import kotlinx.coroutines.flow.first
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ExportWorker(private var context: Context, private var params: WorkerParameters) : CoroutineWorker(context, params) {
    private lateinit var log: Log

    override suspend fun doWork(): Result {
        log = Log(LogType.EXPORTOPML)
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.newDocument()

            val root = document.createElement("opml")
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rsspire", "http://github.com/kokyett/schemas")
            val head = document.createElement("head")
            val body = document.createElement("body")

            document.appendChild(root)
            root.appendChild(head)
            root.appendChild(body)

            val title = document.createElement("title")
            title.textContent = context.getString(R.string.app_name)
            head.appendChild(title)

            exportCategories(document, body)

            log.writeInformation("Saving OPML file")
            applicationContext.contentResolver.openOutputStream(Uri.parse(params.inputData.getString("URI"))).use { outputStream ->
                try {
                    val transformerFactory = TransformerFactory.newInstance().newTransformer()
                    transformerFactory.setOutputProperty(OutputKeys.INDENT, "yes")
                    transformerFactory.transform(DOMSource(document), StreamResult(outputStream))
                } catch (e: Exception) {
                    log.writeException("Error on saving OPML file")
                    log.writeException(e)
                }
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

    private suspend fun exportCategories(document: Document, body: Element) {
        for (category in ApplicationContext.getCategoryRepository().getAll().asFlow().first()) {
            log.writeInformation("Export feeds for category ${category.name}")
            val outline = document.createElement("outline")
            outline.setAttribute("title", category.name)
            outline.setAttribute("text", category.name)
            body.appendChild(outline)
            exportCategoryFeeds(document, outline, category.id)
        }
        log.writeInformation("Export feeds without category")
        exportCategoryFeeds(document, body, null)
    }

    private suspend fun exportCategoryFeeds(document: Document, node: Element, id: Long?) {
        for (feed in ApplicationContext.getFeedRepository().getExportByCategory(id).first()) {
            log.writeInformation("-- Export feed ${feed.url}")
            val outline = document.createElement("outline")
            outline.setAttribute("xmlUrl", feed.url)
            if (feed.title != null) {
                outline.setAttribute("title", feed.title)
                outline.setAttribute("text", feed.title)
            }
            if (feed.iconUrl != null) {
                outline.setAttribute("rsspire:iconUrl", feed.iconUrl)
            }
            outline.setAttribute("rsspire:refreshInterval", DateTime.encodeDelay(feed.refreshInterval))
            outline.setAttribute("rsspire:deleteReadEntriesInterval", DateTime.encodeDelay(feed.deleteReadEntriesInterval))
            outline.setAttribute("rsspire:replaceThumbnails", if (feed.replaceThumbnails) "true" else "false")
            outline.setAttribute("rsspire:downloadFullContent", if (feed.downloadFullContent) "true" else "false")
            node.appendChild(outline)
        }
    }
}