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
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.CategoryViewPagerAdapter
import fr.kokyett.rsspire.fragments.EntriesFragment
import fr.kokyett.rsspire.models.CategoryTabInfo
import fr.kokyett.rsspire.utils.DateTime
import fr.kokyett.rsspire.workers.Workers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var displayUnreadEntries: Boolean = true
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: CategoryViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        Workers.setPeriodicRefreshFeeds(this)

        viewPager = findViewById(R.id.viewpager)
        adapter = CategoryViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false

        val tabLayout = findViewById<TabLayout>(R.id.tablayout)
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = adapter.getText(position) ?: resources.getString(R.string.feeds_no_category)
        }
        tabLayoutMediator.attach()

        displayUnreadEntries = ApplicationContext.getBooleanPreference("pref_display_unread_entries", true)
        ApplicationContext.getPreferencesRepository().setOnlyDisplayUnreadEntries(displayUnreadEntries)
        ApplicationContext.getPreferencesRepository().getCategories().observe(this) { categories ->
            val tabs: ArrayList<CategoryTabInfo> = ArrayList()
            for (category in categories) {
                tabs.add(CategoryTabInfo(EntriesFragment::class.java, category.id, category.name))
            }
            adapter.update(tabs)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
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
            R.id.action_menu_unread -> {
                displayUnreadEntries = !displayUnreadEntries
                ApplicationContext.setBooleanPreference("pref_display_unread_entries", displayUnreadEntries)
                ApplicationContext.getPreferencesRepository().setOnlyDisplayUnreadEntries(displayUnreadEntries)
                updateDisplayUnread(item)
            }
            R.id.action_menu_marl_all_as_read -> {
                val position = viewPager.currentItem
                if (position >= 0) {
                    val categoryId = adapter.getCategoryId(position)
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            ApplicationContext.getEntryRepository().markAllAsReadByCategory(categoryId)
                        }
                    }
                }
            }
            R.id.action_menu_add_feed -> startActivity(Intent(this, SearchFeedActivity::class.java))
            R.id.action_menu_feed_list -> startActivity(Intent(this, FeedsActivity::class.java))
            R.id.action_menu_import_opml -> importStartForResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                })

            R.id.action_menu_export_opml -> exportStartForResult.launch(Intent(Intent.ACTION_CREATE_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "RSSpire${DateTime.now("yyyy-MM-dd-HH-mm-ss-SSS")}.opml")
                })

            R.id.action_menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_menu_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
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

    private fun updateDisplayUnread(item: MenuItem) {
        if (displayUnreadEntries) {
            item.setIcon(R.drawable.ic_action_unread)
        } else {
            item.setIcon(R.drawable.ic_action_read)
        }
    }
}
