package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.RSSpireApplication
import fr.kokyett.rsspire.adapters.IconListAdapter
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.models.FeedIcon
import fr.kokyett.rsspire.utils.ExtrasUtils
import fr.kokyett.rsspire.utils.HtmlUtils
import fr.kokyett.rsspire.views.InstantAutoComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class EditFeedActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var editUrl: EditText
    private lateinit var editTitle: EditText
    private lateinit var editCategory: InstantAutoComplete
    private lateinit var feed: Feed

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feed)

        imageView = findViewById(R.id.imageview)
        editUrl = findViewById(R.id.url)
        editTitle = findViewById(R.id.title)
        editCategory = findViewById(R.id.category)
        editCategory.threshold = 0

        val context = applicationContext as RSSpireApplication
        context.categoryRepository.allCategories.observe(this) { categories ->
            val array = mutableListOf<String>()
            for (value in categories) array.add(value.name)
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array)
            editCategory.setAdapter(adapter)
        }

        if (intent.hasExtra(ExtrasUtils.FEED)) {
            initFromFeed()
        } else if (intent.hasExtra(ExtrasUtils.URL)) {
            editUrl.setText(intent.extras?.getString(ExtrasUtils.URL))
            editTitle.setText(intent.extras?.getString(ExtrasUtils.TITLE))
            feed = Feed(0, null, editUrl.text.toString(), nullIfEmpty(editTitle.text.toString()))
        } else {
            feed = Feed()
        }
    }

    private fun initFromFeed() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val application = (applicationContext as RSSpireApplication)
                feed = application.feedRepository.get(intent.extras?.getLong(ExtrasUtils.FEED) ?: 0) ?: Feed()
                val idCategory = feed.idCategory
                val category = if (idCategory != null) application.categoryRepository.get(idCategory)?.name
                else null

                withContext(Dispatchers.Main) {
                    editUrl.setText(feed.url)
                    editTitle.setText(feed.title)
                    editCategory.setText(category)
                    val icon = feed.icon
                    if (icon != null) imageView.setImageBitmap(BitmapFactory.decodeByteArray(icon, 0, icon.size))
                }
            }
        }
    }

    private fun showDialogIcons(icons: ArrayList<FeedIcon>) {
        val builderSingle = AlertDialog.Builder(this@EditFeedActivity)
        builderSingle.setIcon(R.drawable.ic_action_get_icon)
        builderSingle.setTitle(R.string.edit_feed_select_icon)

        val arrayAdapter = IconListAdapter(this@EditFeedActivity, icons)
        builderSingle.setNegativeButton(R.string.edit_feed_select_icon_cancel) { dialog, _ -> dialog.dismiss() }
        builderSingle.setAdapter(arrayAdapter) { _, which ->
            val feedIcon = arrayAdapter.getItem(which)
            if (feedIcon?.byteArray != null) {
                feed.icon = feedIcon.byteArray
                val bitmap = BitmapFactory.decodeByteArray(feedIcon.byteArray, 0, feedIcon.byteArray!!.size)
                imageView.setImageBitmap(bitmap)
            }
        }
        builderSingle.show()
    }

    private fun getIcons() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (URLUtil.isValidUrl(editUrl.text.toString())) {
                    val url = URL(editUrl.text.toString())
                    val icons = HtmlUtils.getIcons(url.protocol + "://" + url.host)
                    withContext(Dispatchers.Main) {
                        when (icons.size) {
                            0 -> Toast.makeText(this@EditFeedActivity, applicationContext.getText(R.string.edit_feed_not_found_icons), Toast.LENGTH_LONG).show()
                            else -> showDialogIcons(icons)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_feed, menu)
        if (menu != null) MenuCompat.setGroupDividerEnabled(menu, true)
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_menu_delete)
        item.isVisible = feed.id != 0L
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.action_menu_save -> saveFeed()
            R.id.action_menu_search_icon -> getIcons()
            R.id.action_menu_delete -> deleteFeed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteFeed() {
        //TODO delete confirmation popup
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val context = applicationContext as RSSpireApplication
                context.feedRepository.delete(feed)
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }

    private fun saveFeed() {
        if (!URLUtil.isValidUrl(editUrl.text.toString())) {
            Toast.makeText(
                this, resources.getString(R.string.edit_feed_invalid_url), Toast.LENGTH_LONG
            ).show()
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val context = applicationContext as RSSpireApplication
                if (context.feedRepository.urlExists(feed.id, editUrl.text.toString())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditFeedActivity, resources.getString(R.string.edit_feed_url_already_exists), Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    var idCategory: Long? = null
                    if (editCategory.text.toString().trim() != "") idCategory = context.categoryRepository.get(editCategory.text.toString()).id

                    feed.idCategory = idCategory
                    feed.url = editUrl.text.toString()
                    feed.title = nullIfEmpty(editTitle.text.toString())

                    context.feedRepository.save(feed)
                    withContext(Dispatchers.Main) {
                        finish()
                    }
                }
            }
        }
    }

    private fun nullIfEmpty(value: String?): String? {
        if (value?.trim() == "") return null
        return value?.trim()
    }
}