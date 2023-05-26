package fr.kokyett.rsspire.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.models.SearchFeedResult
import fr.kokyett.rsspire.utils.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

class SearchFeedAdapter(private val lifecycleScope: LifecycleCoroutineScope) : ListAdapter<SearchFeedResult, SearchFeedAdapter.ItemViewHolder>(ItemsComparator()) {

    var onItemClick: ((SearchFeedResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_search_result, parent, false)
        return ItemViewHolder(view, lifecycleScope)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)

        holder.bind(current)
    }

    inner class ItemViewHolder(itemView: View, private val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageview)
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
            lifecycleScope. launch {
                try {
                    if (searchFeedResult.bitmap == null)
                        searchFeedResult.bitmap = getBitmap(searchFeedResult.iconUrl)
                    setBitmap(searchFeedResult.bitmap)
                } catch (e: Exception) {
                    //TODO: Log exception
                }
            }
        }

        private suspend fun setBitmap(bitmap: Bitmap?) = withContext(Dispatchers.Main) {
            imageView.setImageBitmap(bitmap)
        }

        private suspend fun getBitmap(url: String?) = withContext(Dispatchers.IO) {
            return@withContext Downloader.getBitmap(URL(url))
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
