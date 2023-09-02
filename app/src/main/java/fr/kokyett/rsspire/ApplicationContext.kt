package fr.kokyett.rsspire

import android.app.Application
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import fr.kokyett.rsspire.database.ApplicationDatabase
import fr.kokyett.rsspire.database.repositories.CategoryRepository
import fr.kokyett.rsspire.database.repositories.EntryRepository
import fr.kokyett.rsspire.database.repositories.FeedRepository
import fr.kokyett.rsspire.enums.LogLineType
import fr.kokyett.rsspire.enums.LogType
import fr.kokyett.rsspire.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ApplicationContext : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { ApplicationDatabase.getDatabase(this, applicationScope) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao) }
    private val feedRepository by lazy { FeedRepository(database.feedDao) }
    private val entryRepository by lazy { EntryRepository(database.entryDao) }
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()
        handle = this

        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            Log(LogType.CRASH). use {
                it.writeCrash(e)
            }
            defaultUncaughtHandler?.uncaughtException(thread, e)
        }
    }

    companion object {
        private lateinit var handle: ApplicationContext

        fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
            return if (formatArgs.isNotEmpty())
                handle.getString(resId, formatArgs)
            else
                handle.getString(resId)
        }

        fun getBooleanPreference(key: String, defaultValue: Boolean): Boolean {
            return handle.sharedPreferences.getBoolean(key, defaultValue)
        }

        fun getApplicationScope(): CoroutineScope {
            return handle.applicationScope
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