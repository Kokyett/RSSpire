package fr.kokyett.rsspire.adapters

import android.content.Context
import android.content.Intent
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
import fr.kokyett.rsspire.activities.EditFeedActivity
import fr.kokyett.rsspire.database.entities.FeedInformation
import fr.kokyett.rsspire.enums.FeedType
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString
import fr.kokyett.rsspire.utils.Downloader
import fr.kokyett.rsspire.workers.Workers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.net.URL

class FeedListAdapter(context: Context) : SwipeListAdapter<FeedInformation, FeedListAdapter.ItemViewHolder>(context, ItemsComparator()) {
    var onItemClick: ((FeedInformation) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.view_feed_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onItemSwiped(direction: Int, position: Int) {
        if (position == -1)
            return

        val feed = getItem(position)
        when (direction) {
            ItemTouchHelper.RIGHT -> {
                if (feed.type != FeedType.LOG) {
                    val intent = Intent(context, EditFeedActivity::class.java)
                    intent.putExtra("ID_FEED", feed.id)
                    context.startActivity(intent)
                    notifyItemChanged(position)
                } else {
                    super.onItemSwiped(direction, position)
                }
            }
            ItemTouchHelper.LEFT -> {
                ApplicationContext.getApplicationScope().launch {
                    withContext(Dispatchers.IO) {
                        if (feed.type != FeedType.LOG && feed.entries == 0) {
                            Workers.refreshFeed(context, feed.id)
                        } else if (feed.unread > 0) {
                            feed.unread = 0
                            ApplicationContext.getFeedRepository().markAllAsRead(feed.id)
                        } else {
                            feed.unread = feed.entries
                            ApplicationContext.getFeedRepository().markAllAsUnread(feed.id)
                        }
                        withContext(Dispatchers.Main) {
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

        val feed = getItem(position)
        if (feed.type != FeedType.LOG) {
            itemSwipedCallBack.setSwipe(ItemTouchHelper.RIGHT, R.color.editFeed, R.drawable.ic_action_edit, R.color.colorOnPrimary)
        }

        if (feed.type != FeedType.LOG && feed.entries == 0)
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.refreshFeed, R.drawable.ic_action_refresh, R.color.colorOnPrimary)
        else if (feed.unread > 0)
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.readEntry, R.drawable.ic_action_read, R.color.colorOnPrimary)
        else
            itemSwipedCallBack.setSwipe(ItemTouchHelper.LEFT, R.color.unreadEntry, R.drawable.ic_action_unread, R.color.colorOnPrimary)
        super.onDrawItemSwiped(direction, position)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView1 = itemView.findViewById<TextView>(R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(R.id.text2)
        private val textView3 = itemView.findViewById<TextView>(R.id.text3)
        private val textView4 = itemView.findViewById<TextView>(R.id.text4)
        private val textView5 = itemView.findViewById<TextView>(R.id.text5)
        private val imageView = itemView.findViewById<ImageView>(R.id.imageview)

        fun bind(feed: FeedInformation) {
            itemView.setOnClickListener {
                onItemClick?.invoke(feed)
            }

            if ((feed.title ?: "").trim() == "") {
                textView1.text = feed.url
                textView2.visibility = View.GONE
            } else {
                textView1.text = feed.title
                textView2.text = feed.url
                textView2.visibility = View.VISIBLE
            }
            if ((feed.description ?: "").trim() == "") {
                textView3.visibility = View.GONE
            } else {
                textView3.text = feed.description
                textView3.visibility = View.VISIBLE
            }

            if (feed.lastEntryDate == null) {
                textView4.visibility = View.GONE
            } else {
                textView4.text = feed.lastEntryDate?.toLocalizedString()
                textView4.visibility = View.VISIBLE
            }
            val sb = StringBuilder()
                .append(context.resources.getString(if (feed.entries > 1)  R.string.feeds_entries else R.string.feeds_entry, feed.entries))
            if (feed.unread > 0)
                sb.append(context.resources.getString(if (feed.unread > 1)  R.string.feeds_unread_entries else R.string.feeds_unread_entry, feed.unread))
            textView5.text = sb.toString()

            imageView.setImageResource(R.drawable.ic_default_feed)

            if (feed.icon != null) {
                val bytes = feed.icon
                Glide.with(context).load(bytes).into(imageView)
            } else if (feed.iconUrl != null) {
                ApplicationContext.getApplicationScope().launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val icon = Downloader.getBytes(URL(feed.iconUrl))
                            ApplicationContext.getFeedRepository().updateIcon(feed.id, icon)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    class ItemsComparator : DiffUtil.ItemCallback<FeedInformation>() {
        override fun areItemsTheSame(oldItem: FeedInformation, newItem: FeedInformation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedInformation, newItem: FeedInformation): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.url == newItem.url &&
                    oldItem.title == newItem.title &&
                    oldItem.description == newItem.description &&
                    oldItem.iconUrl == newItem.iconUrl &&
                    oldItem.icon.contentEquals(newItem.icon) &&
                    oldItem.unread == newItem.unread &&
                    oldItem.entries == newItem.entries
        }
    }
}
