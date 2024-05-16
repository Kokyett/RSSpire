package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryActivity : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var informations: TextView
    private lateinit var webView: WebView
    private lateinit var entry: Entry

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        title = findViewById(R.id.title)
        informations = findViewById(R.id.informations)
        webView = findViewById(R.id.webview)

        var idEntry: Long = 0
        if (intent.extras?.containsKey("ID_ENTRY") == true)
            idEntry = intent.extras?.getLong("ID_ENTRY") ?: 0

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                entry = ApplicationContext.getEntryRepository().get(idEntry)
                ApplicationContext.getEntryRepository().markAsRead(idEntry)
                withContext(Dispatchers.Main) {
                    title.text = entry.title
                    informations.text = entry.publishDate?.toLocalizedString()

                    val html = "$HTML_START${entry.content} $HTML_END"
                    webView.setBackgroundColor(Color.TRANSPARENT)
                    webView.settings.loadWithOverviewMode = true
                    webView.settings.javaScriptEnabled = true
                    webView.loadDataWithBaseURL(null, html, "text/html", null, null)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.entry, menu)
        val item = menu.findItem(R.id.action_menu_favorite)
        updateFavoriteIcon(item)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_menu_open -> try {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(entry.link)
                startActivity(i)
            } catch (_: Exception) {
            }
            R.id.action_menu_favorite -> {
                entry.isFavorite = !entry.isFavorite
                updateFavoriteIcon(item)
                ApplicationContext.getApplicationScope().launch {
                    withContext(Dispatchers.IO) {
                        ApplicationContext.getEntryRepository().setFavorite(entry.id, entry.isFavorite)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateFavoriteIcon(item: MenuItem) {
        if (entry.isFavorite) {
            item.setIcon(R.drawable.ic_action_favorite)
        } else {
            item.setIcon(R.drawable.ic_action_notfavorite)
        }
    }

    companion object {
        private const val HTML_START = "<html><head><style>" +
                "body { color: white; text-align: justify; overflow-wrap: break-word; }" +
                "a { color: #8ad0e8; }" +
                "img { max-width: 100%; height: auto; }" +
                "figure { max-width: 100%; height: auto; margin: auto; }" +
                "iframe { max-width: 100%; height: auto; }" +
                "video { max-width: 100%; height: auto; }" +
                "pre { max-width: 100%; overflow-x:scroll; }" +
                "code { background-color: black !important; color: white !important; font-family: monospace !important;}" +
                "</style></head><body>"

        private const val HTML_END = "</body></html>"
    }
}
