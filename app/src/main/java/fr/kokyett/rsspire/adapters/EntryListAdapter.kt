package fr.kokyett.rsspire.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.database.entities.EntryView
import fr.kokyett.rsspire.enums.LogType
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString
import fr.kokyett.rsspire.utils.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EntryListAdapter(context: Context) : SwipeListAdapter<EntryView, EntryListAdapter.ItemViewHolder>(context, ItemsComparator()) {
    var onItemClick: ((EntryView) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.view_enrty_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onItemSwiped(direction: Int, position: Int) {
        if (position == -1)
            return;

        val entry = getItem(position)
        val displayUnreadEntries = ApplicationContext.getBooleanPreference("pref_display_unread_entries", true)
        when (direction) {
            ItemTouchHelper.LEFT -> {
                ApplicationContext.getApplicationScope().launch {
                    withContext(Dispatchers.IO) {
                        entry.isFavorite = !entry.isFavorite
                        ApplicationContext.getEntryRepository().setFavorite(entry.id, entry.isFavorite)
                        withContext(Dispatchers.Main) {
                            if (!displayUnreadEntries || entry.readDate == null || entry.isFavorite)
                                notifyItemChanged(position)
                        }
                    }
                }
            }
            ItemTouchHelper.RIGHT -> {
                ApplicationContext.getApplicationScope().launch {
                    withContext(Dispatchers.IO) {
                        if (entry.readDate == null) {
                            entry.readDate = Date()
                            ApplicationContext.getEntryRepository().markAsRead(entry.id)
                        } else {
                            entry.readDate = null
                            ApplicationContext.getEntryRepository().markAsUnread(entry.id)
                        }
                        withContext(Dispatchers.Main) {
                            if (!displayUnreadEntries || entry.readDate == null || entry.isFavorite)
                                notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    override fun onDrawItemSwiped(direction: Int, position: Int) {
        if (position == -1)
            return;

        val entry = getItem(position)

        if (entry.isFavorite)
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.cardViewNotFavoriteEntry, R.drawable.ic_action_notfavorite, R.color.colorOnPrimary)
        else
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.cardViewFavoriteEntry, R.drawable.ic_action_favorite, R.color.colorOnPrimary)

        if (entry.readDate == null)
            itemSwipedCallBack.setSwipe(ItemTouchHelper.RIGHT, R.color.cardViewReadEntry, R.drawable.ic_action_read, R.color.colorOnPrimary)
        else
            itemSwipedCallBack.setSwipe(ItemTouchHelper.RIGHT, R.color.cardViewUnreadEntry, R.drawable.ic_action_unread, R.color.colorOnPrimary)

        super.onDrawItemSwiped(direction, position)
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView = itemView.findViewById<MaterialCardView>(R.id.cardview)
        private val imageView = itemView.findViewById<ImageView>(R.id.imageview)
        private val textView1 = itemView.findViewById<TextView>(R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(R.id.text2)
        private val textView3 = itemView.findViewById<TextView>(R.id.text3)
        fun bind(entryView: EntryView) {
            itemView.setOnClickListener {
                onItemClick?.invoke(entryView)
            }

            textView1.text = entryView.title

            if ((entryView.feedTitle?.trim() ?: "") == "") {
                textView2.text = entryView.feedUrl
            } else {
                textView2.text = entryView.feedTitle
            }

            entryView.publishDate?.let {
                textView3.text = it.toLocalizedString()
            }

            if (entryView.isFavorite)
                cardView.strokeColor = context.getColor(R.color.cardViewFavoriteEntry)
            else if (entryView.readDate == null)
                cardView.strokeColor = context.getColor(R.color.cardViewUnreadEntry)
            else
                cardView.strokeColor = context.getColor(R.color.cardViewReadEntry)

            imageView.setImageResource(R.drawable.ic_default_feed)

            ApplicationContext.getApplicationScope().launch {
                withContext(Dispatchers.IO) {
                    val icons = ApplicationContext.getEntryRepository().getIcons(entryView.id)
                    if (icons != null) {
                        if (icons.entryIcon != null) {
                            withContext(Dispatchers.Main) {
                                Glide.with(context).load(icons.entryIcon).into(imageView)
                            }
                        } else if (icons.feedIcon != null) {
                            withContext(Dispatchers.Main) {
                                Glide.with(context).load(icons.feedIcon).into(imageView)
                            }
                        }
                    }
                }
            }
        }
    }

    class ItemsComparator : DiffUtil.ItemCallback<EntryView>() {
        override fun areItemsTheSame(oldItem: EntryView, newItem: EntryView): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EntryView, newItem: EntryView): Boolean {
            return oldItem.id == newItem.id &&
                oldItem.guid == newItem.guid &&
                oldItem.link == newItem.link &&
                oldItem.title == newItem.title &&
                oldItem.feedUrl == newItem.feedUrl &&
                oldItem.feedTitle == newItem.feedTitle &&
                oldItem.isFavorite == newItem.isFavorite &&
                oldItem.publishDate == newItem.publishDate &&
                oldItem.readDate == newItem.readDate
        }
    }
}
