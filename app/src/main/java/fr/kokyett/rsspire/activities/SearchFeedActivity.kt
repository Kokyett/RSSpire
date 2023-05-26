package fr.kokyett.rsspire.activities

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.SearchFeedAdapter
import fr.kokyett.rsspire.models.SearchFeedResult
import fr.kokyett.rsspire.utils.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern


class SearchFeedActivity : AppCompatActivity() {
    private lateinit var adapter: SearchFeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initRecyclerView()
        initEditText()
    }

    private fun initRecyclerView() {
        adapter = SearchFeedAdapter(lifecycleScope)
        adapter.onItemClick = {
            Toast.makeText(this, it.url, Toast.LENGTH_LONG).show()
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
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    val list = search(editText.text.toString())
                    updateListAdapter(list)
                }
            }
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
            val content = Downloader.getString(URL(url))

            val patternHref =
                Pattern.compile("href=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val patternLinkRss = Pattern.compile(
                "<link[^>]*type=[\"']application/rss\\+xml[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE or Pattern.DOTALL
            )
            val matcher = patternLinkRss.matcher(content)
            while (matcher.find()) {
                val hrefMatcher: Matcher = patternHref.matcher(matcher.group(0) ?: "")
                if (hrefMatcher.find()) {
                    val searchFeedResult = SearchFeedResult(
                        hrefMatcher.group(1) ?: "", null, null
                    )
                    list.add(searchFeedResult)
                }
            }
            if (list.size == 0)
                list.add(SearchFeedResult(url, null, null))
        } catch (e: Exception) {
           //TODO: log exception ?
        }
        list.addAll(searchFromFeedly(url))
        return list
    }

    private fun searchFromFeedly(term: String): ArrayList<SearchFeedResult> {
        val uri =
            Uri.Builder().scheme("https").authority("cloud.feedly.com").path("/v3/search/feeds")
                .appendQueryParameter("count", "30")
                .appendQueryParameter("locale", resources.configuration.locales.get(0).language)
                .appendQueryParameter("query", term).build().toString()

        val jsonString = Downloader.getString(URL(uri))
        val json = JSONObject(jsonString)

        val list = ArrayList<SearchFeedResult>()
        val entries = json.getJSONArray("results")
        for (i in 0 until entries.length()) {
            val entry = entries.get(i) as JSONObject
            val searchFeedResult = SearchFeedResult(
                entry.get("feedId").toString().replaceFirst("feed/", ""),
                entry.get("title").toString(),
                if (entry.has("iconUrl")) entry.get("iconUrl").toString() else null
            )
            list.add(searchFeedResult)
        }
        return list
    }
}