package fr.kokyett.rsspire.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.enums.FeedType

class FeedListAdapter(context: Context) : SwipeListAdapter<Feed, FeedListAdapter.ItemViewHolder>(context, ItemsComparator()) {
    var onItemClick: ((Feed) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(android.R.layout.two_line_list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView1 = itemView.findViewById<TextView>(android.R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(android.R.id.text2)

        fun bind(feed: Feed) {
            itemView.setOnClickListener {
                onItemClick?.invoke(feed)
            }

            if ((feed.title ?: "").trim() == "" || feed.type == FeedType.LOG) {
                textView1.text = feed.url
                textView2.visibility = View.GONE
            } else {
                textView1.text = feed.title
                textView2.text = feed.url
                textView2.visibility = View.VISIBLE
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
                    oldItem.iconUrl == newItem.iconUrl &&
                    oldItem.icon.contentEquals(newItem.icon)
        }
    }
}
