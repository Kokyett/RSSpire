package fr.kokyett.rsspire.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.CategoryViewPagerAdapter
import fr.kokyett.rsspire.fragments.FeedsFragment
import fr.kokyett.rsspire.models.CategoryTabInfo

class FeedsActivity : AppCompatActivity() {
    private val tabs: ArrayList<CategoryTabInfo> = ArrayList()
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        val viewPager = findViewById<ViewPager2>(R.id.viewpager)
        viewPager.adapter = CategoryViewPagerAdapter(supportFragmentManager, lifecycle, tabs)

        val tabLayout = findViewById<TabLayout>(R.id.tablayout)
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabs[position].text
        }
        tabLayoutMediator.attach()

        ApplicationContext.getCategoryRepository().getWithFeeds().observe(this) { categories ->
            tabs.clear()
            for (category in categories) {
                tabs.add(CategoryTabInfo(FeedsFragment::class.java, category.id, category.name ?: resources.getString(R.string.feeds_no_category)))
            }
            viewPager.adapter = CategoryViewPagerAdapter(supportFragmentManager, lifecycle, tabs)
            tabLayoutMediator.detach()
            tabLayoutMediator.attach()
        }
    }
}