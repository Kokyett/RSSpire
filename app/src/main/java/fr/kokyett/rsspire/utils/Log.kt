package fr.kokyett.rsspire.utils

import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.BuildConfig
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.enums.LogLineType
import fr.kokyett.rsspire.enums.LogType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Log(private val type: LogType) {
    private val entry: Entry
    private var logs: StringBuilder = StringBuilder()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        logs.append("Type: ${type}\r\n")
        logs.append("Version: ${String.format("%s / %d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)}\r\n")
        logs.append("****************************************\r\n")

        val feed = ApplicationContext.getFeedRepository().getLogsFeed()
        entry = Entry(
            idFeed = feed.id,
            title = when (type) {
                LogType.CRASH -> "Application crash"
                LogType.IMPORTOPML -> "OPML import"
                LogType.EXPORTOPML -> "OPML export"
                LogType.UPDATEFEEDS -> "Update feeds"
                LogType.PARSEDATE -> "Date parsing error"
                else -> "Unknown functionality log"
            },
            content = "",
        )
    }

    fun writeInformation(line: String?) {
        write(LogLineType.INFORMATION, line)
    }

    fun writeInformation(throwable: Throwable) {
        write(LogLineType.INFORMATION, throwable)
    }

    fun writeException(line: String?) {
        write(LogLineType.EXCEPTION, line)
    }

    fun writeException(throwable: Throwable) {
        write(LogLineType.EXCEPTION, throwable)
    }

    fun writeCrash(throwable: Throwable) {
        write(LogLineType.EXCEPTION, throwable)
    }

    private fun write(type: LogLineType, line: String?) {
        logs.append("${dateFormat.format(Date())} [${type}] ${line}\r\n")
    }

    private fun write(type: LogLineType, throwable: Throwable) {
        write(type, throwable.javaClass.name)
        write(type, throwable.message)
        for (stack in throwable.stackTrace) {
            write(type, stack.toString())
        }
    }

    private fun canSaveLog(): Boolean {
        return when (type) {
            LogType.CRASH -> ApplicationContext.getBooleanPreference("pref_log_uncaught_exceptions", true)
            LogType.IMPORTOPML -> ApplicationContext.getBooleanPreference("pref_log_import_opml", false)
            LogType.EXPORTOPML -> ApplicationContext.getBooleanPreference("pref_log_export_opml", false)
            LogType.UPDATEFEEDS -> ApplicationContext.getBooleanPreference("pref_log_update_feeds", true)
            LogType.PARSEDATE -> ApplicationContext.getBooleanPreference("pref_log_parse_date", true)
        }
    }

    fun save() {
        if (canSaveLog()) {
            ApplicationContext.getApplicationScope().launch {
                withContext(Dispatchers.IO) {
                    entry.readDate = null
                    entry.publishDate = Date()
                    entry.content = "<pre>${logs}</pre>"
                    ApplicationContext.getEntryRepository().save(entry)
                }
            }
        }
    }
}