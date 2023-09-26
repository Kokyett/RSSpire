package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import fr.kokyett.rsspire.utils.DateTime
import java.util.concurrent.TimeUnit

class Workers {
    companion object {
        fun importOpml(context: Context, uri: Uri) {
            val data = Data.Builder().putString("URI", uri.toString()).build()
            val request = OneTimeWorkRequestBuilder<ImportWorker>().setInputData(data).build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun exportOpml(context: Context, uri: Uri) {
            val data = Data.Builder().putString("URI", uri.toString()).build()
            val request = OneTimeWorkRequestBuilder<ExportWorker>().setInputData(data).build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun refreshFeeds(context: Context) {
            val request = OneTimeWorkRequestBuilder<RefreshFeedsWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun setPeriodicRefreshFeeds(context: Context) {
            val request = PeriodicWorkRequestBuilder<RefreshFeedsWorker>(DateTime.REFRESH_INTERVAL, TimeUnit.MILLISECONDS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("REFRESH", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}