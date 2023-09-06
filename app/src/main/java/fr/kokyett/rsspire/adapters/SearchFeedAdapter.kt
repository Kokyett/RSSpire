package fr.kokyett.rsspire.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.models.SearchFeedResult

class SearchFeedAdapter(context: Context) : SwipeListAdapter<SearchFeedResult, SearchFeedAdapter.ItemViewHolder>(context, ItemsComparator()) {

    var onItemClick: ((SearchFeedResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.view_search_result, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView1 = itemView.findViewById<TextView>(R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(R.id.text2)

        fun bind(searchFeedResult: SearchFeedResult) {
            itemView.setOnClickListener {
                onItemClick?.invoke(searchFeedResult)
            }

            if ((searchFeedResult.title ?: "").trim() == "") {
                textView1.text = searchFeedResult.url
                textView2.text = ""
            } else {
                textView1.text = searchFeedResult.title
                textView2.text = searchFeedResult.url
            }
        }
    }

    class ItemsComparator : DiffUtil.ItemCallback<SearchFeedResult>() {
        override fun areItemsTheSame(
            oldItem: SearchFeedResult, newItem: SearchFeedResult
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SearchFeedResult,
            newItem: SearchFeedResult
        ): Boolean {
            return oldItem.url == newItem.url
        }
    }
}
