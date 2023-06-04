package fr.kokyett.rsspire.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.utils.DateTimeUtils
import fr.kokyett.rsspire.workers.Workers

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (menu != null) MenuCompat.setGroupDividerEnabled(menu, true)

        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_menu_add_feed -> startActivity(Intent(this, SearchFeedActivity::class.java))
            R.id.action_menu_feed_list -> startActivity(Intent(this, FeedsActivity::class.java))
            R.id.action_menu_import_opml -> importStartForResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                })
            R.id.action_menu_export_opml -> exportStartForResult.launch(Intent(Intent.ACTION_CREATE_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "RSSpire${DateTimeUtils.now("yyyy-MM-dd-HH-mm-ss-SSS")}.opml")
                })
            R.id.action_menu_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private var importStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            Workers.importOpml(applicationContext, result.data?.data!!)
        }
    }

    private var exportStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            Workers.exportOpml(applicationContext, result.data?.data!!)
        }
    }
}