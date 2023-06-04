package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.RSSpireApplication
import kotlinx.coroutines.flow.first
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ExportWorker(private var context: Context, private var params: WorkerParameters) : CoroutineWorker(context, params) {
    private var categoryRepository = (applicationContext as RSSpireApplication).categoryRepository
    private var feedRepository = (applicationContext as RSSpireApplication).feedRepository

    override suspend fun doWork(): Result {
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

            applicationContext.contentResolver.openOutputStream(Uri.parse(params.inputData.getString("URI"))).use {
                outputStream ->
                try {
                    TransformerFactory
                        .newInstance()
                        .newTransformer()
                        .transform(DOMSource(document), StreamResult(outputStream))
                } catch (ex: Exception) {
                    // TODO: Log exception
                }
            }
            Result.success()
        } catch (e: Exception) {
            // TODO: Log error
            Log.e("ExportOPML", "Crash " + e.message, e)
            Result.failure()
        }
    }

    private suspend fun exportCategories(document: Document, body: Element) {
        for (category in categoryRepository.allCategories.asFlow().first()) {
            val outline = document.createElement("outline")
            outline.setAttribute("title", category.name)
            outline.setAttribute("text", category.name)
            body.appendChild(outline)
            exportCategoryFeeds(document, outline, category.id)
        }
        exportCategoryFeeds(document, body, null)
    }

    private suspend fun exportCategoryFeeds(document: Document, node: Element, id: Long?) {
        for (feed in feedRepository.getByCategory(id).first()) {
            val outline = document.createElement("outline")
            outline.setAttribute("xmlUrl", feed.url)
            outline.setAttribute("title", feed.title)
            outline.setAttribute("text", feed.title)
            node.appendChild(outline)
        }
    }
}