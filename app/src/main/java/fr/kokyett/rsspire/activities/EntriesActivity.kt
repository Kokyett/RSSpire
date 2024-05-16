package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.EntryListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntriesActivity : AppCompatActivity() {
    private var idFeed: Long = 0
    private var displayUnreadEntries: Boolean = true
    private lateinit var adapter: EntryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

        if (intent.extras?.containsKey("ID_FEED") == true)
            idFeed = intent.extras?.getLong("ID_FEED") ?: 0

        adapter = EntryListAdapter(this)
        adapter.onItemClick = {
            val intent = Intent(this, EntryActivity::class.java)
            intent.putExtra("ID_ENTRY", it.id)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))

        ApplicationContext.getPreferencesRepository().getEntriesByFeed(idFeed).observe(this) { entries ->
            entries?.let { adapter.submitList(it) }
        }

        displayUnreadEntries = ApplicationContext.getBooleanPreference("pref_display_unread_entries", true)
        ApplicationContext.getPreferencesRepository().setOnlyDisplayUnreadEntries(displayUnreadEntries)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.entries, menu)
        if (menu != null)
            MenuCompat.setGroupDividerEnabled(menu, true)

        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)

        val item = menu?.findItem(R.id.action_menu_unread)
        if (item != null) {
            updateDisplayUnread(item)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.action_menu_unread -> {
                displayUnreadEntries = !displayUnreadEntries
                ApplicationContext.setBooleanPreference("pref_display_unread_entries", displayUnreadEntries)
                ApplicationContext.getPreferencesRepository().setOnlyDisplayUnreadEntries(displayUnreadEntries)
                updateDisplayUnread(item)
            }
            R.id.action_menu_marl_all_as_read -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        ApplicationContext.getEntryRepository().markAllAsReadByFeed(idFeed)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateDisplayUnread(item: MenuItem) {
        if (displayUnreadEntries) {
            item.setIcon(R.drawable.ic_action_unread)
        } else {
            item.setIcon(R.drawable.ic_action_read)
        }
    }
}