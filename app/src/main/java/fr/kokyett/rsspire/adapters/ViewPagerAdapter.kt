package fr.kokyett.rsspire.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.kokyett.rsspire.models.TabInfo

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val tabs: ArrayList<TabInfo>) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return tabs[position].fragmentClass.getConstructor().newInstance() as Fragment
    }

    override fun getItemCount(): Int {
        return tabs.size
    }
}