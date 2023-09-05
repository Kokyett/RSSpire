package fr.kokyett.rsspire.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.utils.DateTime.Companion.toLocalizedString

class EntryListAdapter(context: Context) : SwipeListAdapter<Entry, EntryListAdapter.ItemViewHolder>(context, ItemsComparator()) {
    var onItemClick: ((Entry) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(android.R.layout.two_line_list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView1 = itemView.findViewById<TextView>(android.R.id.text1)
        private val textView2 = itemView.findViewById<TextView>(android.R.id.text2)
        fun bind(entry: Entry) {
            itemView.setOnClickListener {
                onItemClick?.invoke(entry)
            }

            textView1.text = entry.title
            entry.publishDate?.let {
                textView2.text = it.toLocalizedString()
            }
        }
    }

    class ItemsComparator : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.title == newItem.title &&
                    oldItem.content == newItem.content &&
                    oldItem.icon.contentEquals(newItem.icon)
        }
    }
}
