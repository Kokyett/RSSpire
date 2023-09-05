package fr.kokyett.rsspire.activities

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EntryActivity : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var informations: TextView
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        title = findViewById(R.id.title)
        informations = findViewById(R.id.informations)
        webView = findViewById(R.id.webview)

        var idEntry: Long = 0
        if (intent.extras?.containsKey("ID_ENTRY") == true)
            idEntry = intent.extras?.getLong("ID_ENTRY") ?: 0

        ApplicationContext.getEntryRepository().get(idEntry).observe(this) { entry ->
            title.text = entry.title
            informations.text = entry.publishDate?.toLocalizedString()

            val html = HTML_START + entry.content + HTML_END
            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.settings.loadWithOverviewMode = true
            webView.settings.setSupportZoom(true)
            webView.settings.builtInZoomControls = true
            webView.loadDataWithBaseURL(null, html, "text/html", null, null)
        }
    }

    companion object {
        private const val HTML_START = "<html><head><style>" +
                "body { color: white; text-align: justify; }" +
                "a { color: #8ad0e8; }" +
                "img { max-width: 100%; height: auto; }" +
                "</style></head><body>"

        private const val HTML_END = "</body></html>"
    }
}
