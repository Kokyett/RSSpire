package fr.kokyett.rsspire

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import fr.kokyett.rsspire.database.FeedsDatabase
import fr.kokyett.rsspire.database.repositories.CategoryRepository
import fr.kokyett.rsspire.database.repositories.FeedRepository
import fr.kokyett.rsspire.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.io.FileWriter
import java.io.IOException


class RSSpireApplication : Application() {
    private val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { FeedsDatabase.getDatabase(this, applicationScope) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao) }
    val feedRepository by lazy { FeedRepository(database.feedDao) }

    override fun onCreate() {
        super.onCreate()
        handle = this
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            logCrash(e)
            defaultUncaughtHandler?.uncaughtException(thread, e)
        }

    }

    companion object {
        private lateinit var handle: RSSpireApplication

        private enum class Type {
            NONE, CRASH, EXCEPTION, INFORMATION
        }

        private const val ENDLINE = "\r\n"

        @Throws(IOException::class)
        private fun getFileWriter(): FileWriter {
            val file = File(handle.externalCacheDir.toString() + "/" + DateTimeUtils.now("yyyy-MM-dd") + ".log")
            val exists = file.exists()
            val fw = FileWriter(file, true)
            if (!exists) {
                writeHeader(fw)
            }
            return fw
        }

        @Suppress("DEPRECATION")
        private fun writeHeader(fw: FileWriter) {
            val version = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    handle.packageManager.getPackageInfo(handle.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
                } else {
                    handle.packageManager.getPackageInfo(handle.packageName,0).versionName
                }
            } catch (e: java.lang.Exception) {
                ""
            }

            writeLine(fw, "==========================================")
            writeLine(fw, "Package: $handle.packageName")
            writeLine(fw, "Version: $version")
            writeLine(fw, "==========================================")
        }

        @Throws(IOException::class)
        private fun writeLine(fw: FileWriter, message: String) {
            fw.write(message + ENDLINE)
        }

        @Throws(IOException::class)
        private fun writeFullLine(fw: FileWriter, message: String?, type: Type) {
            var fullMessage: String = DateTimeUtils.now("HH:mm:ss")
            if (Type.NONE == type) {
                fullMessage += "              "
            } else if (Type.CRASH == type) {
                fullMessage += " CRASH      : "
            } else if (Type.EXCEPTION == type) {
                fullMessage += " EXCEPTION  : "
            } else if (Type.INFORMATION == type) {
                fullMessage += " INFORMATION: "
            }
            writeLine(fw, fullMessage + message)
        }

        private fun write(message: String, type: Type) {
            try {
                val fw = getFileWriter()
                writeFullLine(fw, message, type)
                fw.close()
            } catch (_: Exception) {
            }
        }

        private fun write(t: Throwable, type: Type) {
            try {
                val fw = getFileWriter()
                writeFullLine(fw, t.message, type)
                for (stack in t.stackTrace) {
                    writeFullLine(fw, stack.toString(), type)
                }
                fw.close()
            } catch (_: Exception) {
            }
        }

        fun logNone(message: String) {
            write(message, Type.NONE)
        }

        fun logNone(t: Throwable) {
            write(t, Type.NONE)
        }

        fun logInformation(message: String) {
            write(message, Type.INFORMATION)
        }

        fun logInformation(t: Throwable) {
            write(t, Type.INFORMATION)
        }

        fun logCrash(t: Throwable) {
            write(t, Type.CRASH)
        }

        fun logException(t: Throwable) {
            write(t, Type.EXCEPTION)
        }
    }
}