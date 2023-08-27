package fr.kokyett.rsspire

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import fr.kokyett.rsspire.database.ApplicationDatabase
import fr.kokyett.rsspire.database.repositories.CategoryRepository
import fr.kokyett.rsspire.database.repositories.EntryRepository
import fr.kokyett.rsspire.database.repositories.FeedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ApplicationContext  : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { ApplicationDatabase.getDatabase(this, applicationScope) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao) }
    private val feedRepository by lazy { FeedRepository(database.feedDao) }
    private val entryRepository by lazy { EntryRepository(database.entryDao) }

    override fun onCreate() {
        super.onCreate()
        handle = this
    }

    companion object {
        private lateinit var handle: ApplicationContext

        fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
            return if (formatArgs.isNotEmpty())
                handle.getString(resId, formatArgs)
            else
                handle.getString(resId)
        }

        fun getCategoryRepository(): CategoryRepository {
            return handle.categoryRepository
        }

        fun getFeedRepository(): FeedRepository {
            return handle.feedRepository
        }

        fun getEntryRepository(): EntryRepository {
            return handle.entryRepository
        }
    }
}