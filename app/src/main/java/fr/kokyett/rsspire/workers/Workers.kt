package fr.kokyett.rsspire.workers

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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
    }
}