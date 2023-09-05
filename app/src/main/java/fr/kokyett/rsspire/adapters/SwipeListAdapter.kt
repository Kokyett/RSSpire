package fr.kokyett.rsspire.adapters

import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R

abstract class SwipeListAdapter<T, VH: RecyclerView.ViewHolder>(protected var context:Context, itemCallback: DiffUtil.ItemCallback<T>)  : ListAdapter<T, VH>(itemCallback) {
    private lateinit var itemTouchHelper: ItemTouchHelper
    protected lateinit var  itemSwipedCallBack: ItemSwipedCallback

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemSwipedCallBack = ItemSwipedCallback(context)
        itemTouchHelper = ItemTouchHelper(itemSwipedCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        itemSwipedCallBack.onItemSwiped = { direction, position -> onItemSwiped(direction, position) }
        itemSwipedCallBack.onDrawItemSwiped = { direction, position -> onDrawItemSwiped(direction, position) }
    }

    open fun onItemSwiped(direction: Int, position: Int) {
        notifyItemChanged(position)
        Toast.makeText(context, R.string.error_swipe_not_implemented, Toast.LENGTH_LONG).show()
    }

    open fun onDrawItemSwiped(direction: Int, position: Int) {
    }
}
