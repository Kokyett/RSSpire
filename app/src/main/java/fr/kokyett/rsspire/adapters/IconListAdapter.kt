package fr.kokyett.rsspire.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.models.FeedIcon
import fr.kokyett.rsspire.utils.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

class IconListAdapter(private val activity: AppCompatActivity, icons: ArrayList<FeedIcon>) : ArrayAdapter<FeedIcon>(activity, R.layout.view_icon_item, icons) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, R.layout.view_icon_item, null)
        val item = getItem(position)
        val imageView = view.findViewById(R.id.imageview) as ImageView
        imageView.visibility = View.GONE

        if (item != null) {
            if (item.byteArray == null) {
                imageView.visibility = View.GONE
                activity.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        item.byteArray = ByteArray(0)
                        try {
                            var bitmap: Bitmap? = null
                            if (URLUtil.isValidUrl(item.url)) {
                                 bitmap = try {
                                    val bytes = Downloader.getBytes(URL(item.url))
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                item.byteArray = stream.toByteArray()
                                stream.close()
                            } else {
                                withContext(Dispatchers.Main) {
                                    this@IconListAdapter.remove(item)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                this@IconListAdapter.remove(item)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            notifyDataSetChanged()
                        }
                    }
                }
            } else if (item.byteArray!!.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(item.byteArray, 0, item.byteArray!!.size)
                if (bitmap != null) {
                    imageView.visibility = View.VISIBLE
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
        return view!!
    }
}