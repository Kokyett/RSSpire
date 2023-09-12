package fr.kokyett.rsspire.utils

import java.text.DateFormat
import java.text.FieldPosition
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class DateTime {
    companion object {
        const val SECOND = 1000L
        const val MINUTE = 60 * SECOND
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
        const val WEEK = 7 * DAY

        fun now(format: String): String {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
        }

        private val secondFieldPosition = FieldPosition(DateFormat.SECOND_FIELD)
        private val fullDateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)

        fun Date.toLocalizedString(): String {
            var sb = StringBuffer()
            sb = fullDateFormat.format(this, sb, secondFieldPosition)
            sb.insert(secondFieldPosition.endIndex, "")
            return sb.toString()
        }
    }
}