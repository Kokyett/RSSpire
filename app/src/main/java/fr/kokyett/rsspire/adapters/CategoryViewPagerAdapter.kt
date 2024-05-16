package fr.kokyett.rsspire.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
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

    override fun getItemId(position: Int): Long {
        return tabs[position].id ?: 0
    }

    override fun containsItem(itemId: Long): Boolean {
        return tabs.any { (it.id ?: 0) == itemId }
    }

    fun getCategoryId(position: Int): Long? {
        return tabs[position].id
    }

    fun getText(position: Int): String? {
        return tabs[position].text
    }

    fun update(newTabs: ArrayList<CategoryTabInfo>) {
        val callback = CategoryDiffUtil(tabs, newTabs)
        val diff = DiffUtil.calculateDiff(callback)
        tabs.clear()
        tabs.addAll(newTabs)
        diff.dispatchUpdatesTo(this)
    }

    private class CategoryDiffUtil(private val oldList: ArrayList<CategoryTabInfo>, private val newList: ArrayList<CategoryTabInfo>) : DiffUtil.Callback() {
        enum class PayloadKey {
            VALUE
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].text?.lowercase() == newList[newItemPosition].text?.lowercase()
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return listOf(PayloadKey.VALUE)
        }
    }
}
