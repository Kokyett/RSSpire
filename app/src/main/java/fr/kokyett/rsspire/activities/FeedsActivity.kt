package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.RSSpireApplication
import fr.kokyett.rsspire.adapters.FeedListAdapter
import fr.kokyett.rsspire.utils.DateTimeUtils
import fr.kokyett.rsspire.utils.ExtrasUtils
import fr.kokyett.rsspire.workers.Workers

class FeedsActivity : AppCompatActivity() {
    private lateinit var adapter: FeedListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeds)
        initRecyclerView()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feed_list, menu)
        if (menu != null) MenuCompat.setGroupDividerEnabled(menu, true)

        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_menu_add_feed -> startActivity(Intent(this, SearchFeedActivity::class.java))
            R.id.action_menu_import_opml -> importStartForResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                })
            R.id.action_menu_export_opml -> exportStartForResult.launch(Intent(Intent.ACTION_CREATE_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "RSSpire${DateTimeUtils.now("yyyy-MM-dd-HH-mm-ss-SSS")}.opml")
                })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        adapter = FeedListAdapter()
        adapter.onItemClick = {
            val intent = Intent(this, EditFeedActivity::class.java)
            intent.putExtra(ExtrasUtils.FEED, it.id)
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

        (application as RSSpireApplication).feedRepository.allFeeds.observe(this) { feeds ->
            feeds?.let { adapter.submitList(it) }
        }
    }

    private var importStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            Workers.importOpml(applicationContext, result.data?.data!!)
        }
    }

    private var exportStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            Workers.exportOpml(applicationContext, result.data?.data!!)
        }
    }
}