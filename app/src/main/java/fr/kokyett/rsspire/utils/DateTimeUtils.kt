package fr.kokyett.rsspire.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeUtils {
    companion object {
        const val SECOND = 1000
        const val MINUTE = 60 * SECOND
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
        const val WEEK = 7 * DAY

        fun now(format: String): String {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
        }
    }
}