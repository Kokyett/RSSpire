package fr.kokyett.rsspire

import android.app.Application
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import fr.kokyett.rsspire.database.ApplicationDatabase
import fr.kokyett.rsspire.database.repositories.CategoryRepository
import fr.kokyett.rsspire.database.repositories.EntryRepository
import fr.kokyett.rsspire.database.repositories.FeedRepository
import fr.kokyett.rsspire.database.repositories.PreferencesRepository
import fr.kokyett.rsspire.database.repositories.RawRepository
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
    private val rawRepository by lazy { RawRepository(database.rawDao) }
    private val preferencesRepository by lazy { PreferencesRepository(database.categoryDao, database.entryDao) }
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()
        handle = this

        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            val log = Log(LogType.CRASH)
            log.writeCrash(e)
            log.save()
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

        fun getStringPreference(key: String, defaultValue: String): String? {
            return handle.sharedPreferences.getString(key, defaultValue)
        }

        fun setBooleanPreference(key: String, value: Boolean) {
            val editor = handle.sharedPreferences.edit()
            editor.putBoolean(key, value)
            editor.apply()
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

        fun getRawRepository(): RawRepository {
            return handle.rawRepository
        }

        fun getPreferencesRepository(): PreferencesRepository {
            return handle.preferencesRepository
        }
    }
}