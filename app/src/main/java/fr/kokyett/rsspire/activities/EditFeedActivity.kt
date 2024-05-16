package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.adapters.IconListAdapter
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.enums.FeedType
import fr.kokyett.rsspire.models.FeedIcon
import fr.kokyett.rsspire.utils.DateTime
import fr.kokyett.rsspire.utils.Downloader
import fr.kokyett.rsspire.utils.Html
import fr.kokyett.rsspire.views.InstantAutoComplete
import fr.kokyett.rsspire.workers.Workers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class EditFeedActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var editUrl: EditText
    private lateinit var editIconUrl: EditText
    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var editCategory: InstantAutoComplete
    private lateinit var spinnerRefreshInterval: Spinner
    private lateinit var spinnerDeleteReadIntervalInterval: Spinner
    private lateinit var checkBoxReplaceThumbnails: CheckBox
    private lateinit var checkBoxDownloadFullContent: CheckBox
    private lateinit var refreshIntervalValues: Array<out String>
    private lateinit var deleteReadEntriesIntervalValues: Array<out String>
    private lateinit var feed: Feed

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feed)

        refreshIntervalValues = resources.getStringArray(R.array.pref_refresh_intervals)
        deleteReadEntriesIntervalValues = resources.getStringArray(R.array.pref_delete_read_entries_intervals)

        imageView = findViewById(R.id.imageview)
        editUrl = findViewById(R.id.url)
        editIconUrl = findViewById(R.id.icon_url)
        editTitle = findViewById(R.id.title)
        editDescription = findViewById(R.id.description)
        editCategory = findViewById(R.id.category)
        spinnerRefreshInterval = findViewById(R.id.spinner_refresh_interval)
        spinnerDeleteReadIntervalInterval = findViewById(R.id.spinner_delete_read_entries_interval)
        checkBoxReplaceThumbnails = findViewById(R.id.check_replace_thumbnails)
        checkBoxDownloadFullContent = findViewById(R.id.check_download_full_content)
        editCategory.threshold = 0


        editIconUrl.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val url = s.toString()
                if (nullIfEmpty(url) != null && !URLUtil.isValidUrl(url))
                    return

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val download = Downloader.getBytes(URL(url))
                            if (download.isNotEmpty()) {
                                feed.icon = download
                                feed.iconUrl = url
                                withContext(Dispatchers.Main) {
                                    val icon = feed.icon
                                    if (icon != null) imageView.setImageBitmap(BitmapFactory.decodeByteArray(icon, 0, icon.size))
                                }
                            }
                        } catch (e: Exception) {
                            // Nothing to do
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })


        ApplicationContext.getCategoryRepository().getAll().observe(this) { categories ->
            val array = mutableListOf<String>()
            for (value in categories) array.add(value.name)
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array)
            editCategory.setAdapter(adapter)
        }

        if (intent.hasExtra("ID_FEED")) {
            initFromFeed()
        } else if (intent.hasExtra("URL")) {
            editUrl.setText(intent.extras?.getString("URL"))
            editTitle.setText(intent.extras?.getString("TITLE"))
            feed = Feed(
                id = 0,
                idCategory = null,
                type = FeedType.RSS,
                url = editUrl.text.toString(),
                title = nullIfEmpty(editTitle.text.toString())
            )
            initSpinners()
        } else {
            feed = Feed()
            initSpinners()
        }
    }

    private fun initFromFeed() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                feed = ApplicationContext.getFeedRepository().get(intent.extras?.getLong("ID_FEED") ?: 0) ?: Feed()
                val idCategory = feed.idCategory
                val category = if (idCategory != null) ApplicationContext.getCategoryRepository().get(idCategory)?.name
                else null

                if (feed.icon == null && feed.iconUrl != null) {
                    if (URLUtil.isValidUrl(feed.iconUrl)) {
                        try {
                            feed.icon = Downloader.getBytes(URL(feed.iconUrl))
                            ApplicationContext.getFeedRepository().save(feed)
                        } catch (e: Exception) {
                            // Nothing to do
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    editUrl.setText(feed.url)
                    editIconUrl.setText(feed.iconUrl)
                    editTitle.setText(feed.title)
                    editDescription.setText(feed.description)
                    editCategory.setText(category)
                    checkBoxReplaceThumbnails.isChecked = feed.replaceThumbnails
                    checkBoxDownloadFullContent.isChecked = feed.downloadFullContent

                    initSpinners()

                    val icon = feed.icon
                    if (icon != null) imageView.setImageBitmap(BitmapFactory.decodeByteArray(icon, 0, icon.size))
                }
            }
        }
    }

    private fun initSpinners() {
        for (i in refreshIntervalValues.indices) {
            if (DateTime.decodeDelay(refreshIntervalValues[i]) == feed.refreshInterval) {
                spinnerRefreshInterval.setSelection(i)
                break
            }
        }

        for (i in deleteReadEntriesIntervalValues.indices) {
            if (DateTime.decodeDelay(deleteReadEntriesIntervalValues[i]) == feed.deleteReadEntriesInterval) {
                spinnerDeleteReadIntervalInterval.setSelection(i)
                break
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
                feed.iconUrl = feedIcon.url
                editIconUrl.setText(feedIcon.url)

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
                    val icons = Html.getIcons(url.protocol + "://" + url.host)
                    withContext(Dispatchers.Main) {
                        when (icons.size) {
                            0 -> Toast.makeText(
                                this@EditFeedActivity,
                                applicationContext.getText(R.string.edit_feed_not_found_icons),
                                Toast.LENGTH_LONG
                            ).show()

                            else -> showDialogIcons(icons)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feed, menu)
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
            R.id.action_menu_reinitialize -> reinitializeFeed()
            R.id.action_menu_delete -> deleteFeed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun reinitializeFeed() {
        feed.lastEntryDate = null
        feed.nextRefreshDate = null
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ApplicationContext.getFeedRepository().reinitialize(feed.id)
                ApplicationContext.getEntryRepository().deleteFeedEntries(feed.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditFeedActivity,
                        resources.getString(R.string.edit_feed_reinitialized),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun deleteFeed() {
        //TODO confirmation popup
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ApplicationContext.getFeedRepository().delete(feed)
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

        if (editIconUrl.text.toString() != "") {
            if (!URLUtil.isValidUrl(editIconUrl.text.toString())) {
                Toast.makeText(
                    this, resources.getString(R.string.edit_feed_invalid_icon_url), Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (ApplicationContext.getFeedRepository().urlExists(feed.id, editUrl.text.toString())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditFeedActivity, resources.getString(R.string.edit_feed_url_already_exists), Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    var idCategory: Long? = null
                    if (editCategory.text.toString().trim() != "") idCategory =
                        ApplicationContext.getCategoryRepository().get(editCategory.text.toString()).id

                    feed.idCategory = idCategory
                    feed.url = editUrl.text.toString()
                    feed.title = nullIfEmpty(editTitle.text.toString())
                    feed.description = nullIfEmpty(editDescription.text.toString())
                    feed.refreshInterval = DateTime.decodeDelay(refreshIntervalValues[spinnerRefreshInterval.selectedItemPosition])
                    feed.deleteReadEntriesInterval =
                        DateTime.decodeDelay(deleteReadEntriesIntervalValues[spinnerDeleteReadIntervalInterval.selectedItemPosition])
                    feed.nextRefreshDate = null
                    feed.replaceThumbnails = checkBoxReplaceThumbnails.isChecked
                    feed.downloadFullContent = checkBoxDownloadFullContent.isChecked

                    ApplicationContext.getFeedRepository().save(feed)
                    withContext(Dispatchers.Main) {
                        Workers.refreshFeed(this@EditFeedActivity, feed.id)
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
