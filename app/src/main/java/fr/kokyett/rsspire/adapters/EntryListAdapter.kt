package fr.kokyett.rsspire.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.database.entities.EntryView
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString
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
            return

        val entry = getItem(position)
        val displayUnreadEntries = ApplicationContext.getBooleanPreference("pref_display_unread_entries", true)
        when (direction) {
            ItemTouchHelper.RIGHT -> {
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
            ItemTouchHelper.LEFT -> {
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
            return

        val entry = getItem(position)

        if (entry.isFavorite)
            itemSwipedCallBack.setSwipe(ItemTouchHelper.RIGHT, R.color.notFavoriteEntry, R.drawable.ic_action_notfavorite, R.color.colorOnPrimary)
        else
            itemSwipedCallBack.setSwipe(ItemTouchHelper.RIGHT, R.color.favoriteEntry, R.drawable.ic_action_favorite, R.color.colorOnPrimary)

        if (entry.readDate == null)
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.readEntry, R.drawable.ic_action_read, R.color.colorOnPrimary)
        else
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.unreadEntry, R.drawable.ic_action_unread, R.color.colorOnPrimary)

        super.onDrawItemSwiped(direction, position)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageview)
        private val textView1 = itemView.findViewById<TextView>(R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(R.id.text2)

        fun bind(entryView: EntryView) {
            itemView.setOnClickListener {
                onItemClick?.invoke(entryView)
            }

            textView1.text = Html.fromHtml(entryView.title, Html.FROM_HTML_MODE_COMPACT)
            if (entryView.isFavorite)
                textView1.setTextColor(context.getColor(R.color.favoriteEntry))
            else if (entryView.readDate == null)
                textView1.setTextColor(context.getColor(R.color.unreadEntry))
            else
                textView1.setTextColor( context.getColor(R.color.colorOnPrimary))

            entryView.publishDate?.let {
                textView2.text = it.toLocalizedString()
            }

            imageView.setImageResource(R.drawable.ic_default_feed)

            ApplicationContext.getApplicationScope().launch {
                withContext(Dispatchers.IO) {
                    val icons = ApplicationContext.getEntryRepository().getIcons(entryView.id)
                    if (icons != null) {
                        if (icons.feedIcon != null) {
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
