package fr.kokyett.rsspire.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.kokyett.rsspire.models.CategoryTabInfo

class CategoryViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private var tabs: ArrayList<CategoryTabInfo> = ArrayList()

    override fun createFragment(position: Int): Fragment {
        val fragment = tabs[position].fragmentClass.getConstructor().newInstance() as Fragment
        fragment.arguments = Bundle()
        tabs[position].id?.let { fragment.requireArguments().putLong("ID_CATEGORY", it) }
        return fragment
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun getText(position: Int): String? {
        return tabs[position].text
    }

    fun update(newTabs: ArrayList<CategoryTabInfo>) {
        for (i in 0..<newTabs.size) {
            var tab = tabs.find { it.id == newTabs[i].id }
            if (tab == null) {
                tab = tabs.find { (it.text?.lowercase() ?: "") > (newTabs[i].text?.lowercase() ?: "") }
                val pos = if (tab == null) tabs.size else tabs.indexOf(tab)
                tabs.add(pos, newTabs[i])
                notifyItemInserted(pos)
            } else {
                if (tab.text?.lowercase() != newTabs[i].text?.lowercase()) {
                    tab.text = newTabs[i].text
                    notifyItemChanged(tabs.indexOf(tab))
                }
            }
        }
    }
}