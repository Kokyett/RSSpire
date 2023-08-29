package fr.kokyett.rsspire.utils

import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.BuildConfig
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.enums.LogLineType
import fr.kokyett.rsspire.enums.LogType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Log(val type: LogType): Closeable {
    private var logs: StringBuilder = StringBuilder()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        logs.append("<pre>")
        logs.append("Type: ${type}\r\n")
        logs.append("Version: ${String.format("%s / %d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)}\r\n")
        logs.append("****************************************\r\n")
    }

    fun write(type: LogLineType, line: String?) {
        logs.append("${dateFormat.format(Date())} [${type}] ${line}\r\n")
    }

    fun write(type: LogLineType, throwable: Throwable) {
        write(type, throwable.javaClass.name)
        write(type, throwable.message)
        for (stack in throwable.stackTrace) {
            write(type, stack.toString())
        }
    }

    private fun canSaveLgg(): Boolean {
        return when (type) {
            LogType.CRASH -> ApplicationContext.getBooleanPreference("pref_uncaught_exceptions", true)
            else -> true;
        }
    }

    override fun close() {
        logs.append("</pre>")
        if (canSaveLgg()) {
            ApplicationContext.getApplicationScope().launch {
                withContext(Dispatchers.IO) {
                    val feed = ApplicationContext.getFeedRepository().getLogsFeed()
                    val entry = Entry(
                        idFeed = feed.id,
                        title = when (type) {
                            LogType.CRASH -> "Application crash"
                            else -> "Unknown functionality log"
                        },
                        content = "",
                        publishDate = Date()
                    )
                    entry.content = logs.toString()
                    ApplicationContext.getEntryRepository().save(entry)
                }
            }
        }
    }
}