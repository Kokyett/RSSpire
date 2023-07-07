package fr.kokyett.rsspire.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.RSSpireApplication
import fr.kokyett.rsspire.database.entities.Feed

class FeedListAdapter : ListAdapter<Feed, FeedListAdapter.ItemViewHolder>(ItemsComparator()) {

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
        private val imageView = itemView.findViewById<ImageView>(R.id.imageview)

        fun bind(feed: Feed) {
            itemView.setOnClickListener {
                onItemClick?.invoke(feed)
            }

            if ((feed.title ?: "").trim() == "") {
                textView1.text = feed.url
                textView2.text = ""
            } else {
                textView1.text = feed.title
                textView2.text = feed.url
            }
            imageView.setImageResource(R.drawable.ic_default_feed)

            val icon = feed.icon
            if (icon != null) {
                try {
                    val bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.size)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    RSSpireApplication.logException(e)
                }
            }
        }
    }

    class ItemsComparator : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.url == newItem.url
        }
    }
}
