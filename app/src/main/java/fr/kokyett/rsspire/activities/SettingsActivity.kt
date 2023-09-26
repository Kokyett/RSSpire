package fr.kokyett.rsspire.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.ViewPagerAdapter
import fr.kokyett.rsspire.fragments.FeedsPreferencesFragment
import fr.kokyett.rsspire.fragments.LogsPreferencesFragment
import fr.kokyett.rsspire.models.TabInfo

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        val tabs: ArrayList<TabInfo> = ArrayList()
        tabs.add(TabInfo(FeedsPreferencesFragment::class.java, resources.getString(R.string.feeds_tab), R.drawable.ic_tab_feeds))
        tabs.add(TabInfo(LogsPreferencesFragment::class.java, resources.getString(R.string.logs_tab), R.drawable.ic_tab_logs))

        val viewPager = findViewById<ViewPager2>(R.id.viewpager)
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, tabs)

        val tabLayout = findViewById<TabLayout>(R.id.tablayout)
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tabs[position].iconId?.let { tab.setIcon(it) }
            tab.text = tabs[position].text
        }.attach()
    }
}