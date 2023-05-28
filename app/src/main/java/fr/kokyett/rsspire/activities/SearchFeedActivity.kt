package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.SearchFeedAdapter
import fr.kokyett.rsspire.models.SearchFeedResult
import fr.kokyett.rsspire.utils.DateTimeUtils
import fr.kokyett.rsspire.utils.DownloaderUtils
import fr.kokyett.rsspire.utils.ExtrasUtils
import fr.kokyett.rsspire.utils.HtmlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.regex.Matcher


class SearchFeedActivity : AppCompatActivity() {
    private lateinit var adapter: SearchFeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initRecyclerView()
        initEditText()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        adapter = SearchFeedAdapter()
        adapter.onItemClick = {
            val intent = Intent(this, EditFeedActivity::class.java)
            intent.putExtra(ExtrasUtils.URL, it.url)
            intent.putExtra(ExtrasUtils.TITLE, it.title)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context, DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun initEditText() {
        val editText = findViewById<EditText>(R.id.edittext)
        editText.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()

            override fun afterTextChanged(s: Editable) {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            lifecycleScope.launch {
                                val list = search(s.toString())
                                updateListAdapter(list)
                            }
                        }
                    },
                    DateTimeUtils.SECOND.toLong()
                )
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private suspend fun updateListAdapter(list: ArrayList<SearchFeedResult>) =
        withContext(Dispatchers.Main) {
            adapter.submitList(list)
        }

    private suspend fun search(term: String): ArrayList<SearchFeedResult> =
        withContext(Dispatchers.IO) {
            try {
                if (URLUtil.isValidUrl(term)) {
                    return@withContext searchFromURL(term)
                } else {
                    return@withContext searchFromFeedly(term)
                }
            } catch (e: Exception) {
                return@withContext ArrayList<SearchFeedResult>()
            }
        }

    private fun searchFromURL(url: String): ArrayList<SearchFeedResult> {
        val list = ArrayList<SearchFeedResult>()
        try {
            var content = DownloaderUtils.getString(url) ?: ""
            content = HtmlUtils.restoreLinks(url, content)

            val matcher = HtmlUtils.patternLinkRss.matcher(content)
            while (matcher.find()) {
                val hrefMatcher: Matcher = HtmlUtils.patternHref.matcher(matcher.group(0) ?: "")
                if (hrefMatcher.find()) {
                    val searchFeedResult = SearchFeedResult(hrefMatcher.group(1) ?: "")
                    list.add(searchFeedResult)
                }
            }
        } catch (e: Exception) {
            //TODO: log exception ?
        }
        if (list.size == 0)
            list.add(SearchFeedResult(url, null))
        list.addAll(searchFromFeedly(url))
        return list
    }

    private fun searchFromFeedly(term: String): ArrayList<SearchFeedResult> {
        val uri =
            Uri.Builder().scheme("https").authority("cloud.feedly.com").path("/v3/search/feeds")
                .appendQueryParameter("count", "30")
                .appendQueryParameter("locale", resources.configuration.locales.get(0).language)
                .appendQueryParameter("query", term).build().toString()

        val list = ArrayList<SearchFeedResult>()
        val jsonString = DownloaderUtils.getString(uri) ?: return list
        val json = JSONObject(jsonString)

        val entries = json.getJSONArray("results")
        for (i in 0 until entries.length()) {
            val entry = entries.get(i) as JSONObject
            val searchFeedResult = SearchFeedResult(
                entry.get("feedId").toString().replaceFirst("feed/", ""),
                entry.get("title").toString(),
            )
            list.add(searchFeedResult)
        }
        return list
    }
}