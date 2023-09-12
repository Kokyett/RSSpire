package fr.kokyett.rsspire.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString
import fr.kokyett.rsspire.utils.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class FeedListAdapter(context: Context) : SwipeListAdapter<Feed, FeedListAdapter.ItemViewHolder>(context, ItemsComparator()) {
    var onItemClick: ((Feed) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.view_feed_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView1 = itemView.findViewById<TextView>(R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(R.id.text2)
        private val textView3 = itemView.findViewById<TextView>(R.id.text3)
        private val textView4 = itemView.findViewById<TextView>(R.id.text4)
        private val imageView = itemView.findViewById<ImageView>(R.id.imageview)

        fun bind(feed: Feed) {
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

            imageView.setImageResource(R.drawable.ic_default_feed)

            if (feed.icon != null) {
                val bytes = feed.icon
                Glide.with(context).load(bytes).into(imageView);
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

    class ItemsComparator : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.url == newItem.url &&
                    oldItem.title == newItem.title &&
                    oldItem.description == newItem.description &&
                    oldItem.iconUrl == newItem.iconUrl &&
                    oldItem.icon.contentEquals(newItem.icon)
        }
    }
}
