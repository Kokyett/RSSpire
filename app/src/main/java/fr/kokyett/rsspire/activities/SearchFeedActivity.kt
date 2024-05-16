package fr.kokyett.rsspire.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.SearchFeedAdapter
import fr.kokyett.rsspire.models.SearchFeedResult
import fr.kokyett.rsspire.utils.Downloader
import fr.kokyett.rsspire.utils.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SearchFeedActivity : AppCompatActivity() {
    private lateinit var adapter: SearchFeedAdapter
    private lateinit var editText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_feed)

        editText = findViewById(R.id.edittext)
        button = findViewById(R.id.button)
        button.setOnClickListener {
            lifecycleScope.launch {
                val list = search(editText.text.toString())
                updateListAdapter(list)
            }

        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = SearchFeedAdapter(this)
        adapter.onItemClick = {
            val intent = Intent(this, EditFeedActivity::class.java)
            intent.putExtra("URL", it.url)
            intent.putExtra("TITLE", it.title)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
            var content = Downloader.getString(url) ?: ""
            content = Html.restoreLinks(url, content)
            list.addAll(Html.getRssLinks(content))
        } catch (_: Exception) {

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
        val jsonString = Downloader.getString(uri) ?: return list
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