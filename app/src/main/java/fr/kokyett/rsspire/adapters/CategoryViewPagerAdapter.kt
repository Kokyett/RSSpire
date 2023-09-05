package fr.kokyett.rsspire.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.kokyett.rsspire.models.CategoryTabInfo

class CategoryViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val tabs: ArrayList<CategoryTabInfo>) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        val fragment = tabs[position].fragmentClass.getConstructor().newInstance() as Fragment
        fragment.arguments = Bundle()
        tabs[position].id?.let { fragment.requireArguments().putLong("ID_CATEGORY", it) }
        return fragment
    }
    override fun getItemCount(): Int {
        return tabs.size
    }
}