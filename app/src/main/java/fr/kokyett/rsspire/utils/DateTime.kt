package fr.kokyett.rsspire.utils

import java.text.DateFormat
import java.text.FieldPosition
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.regex.Pattern

class DateTime {
    companion object {
        const val SECOND = 1000L
        const val MINUTE = 60 * SECOND
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
        const val WEEK = 7 * DAY
        const val REFRESH_INTERVAL = 15 * MINUTE

        private val encodingMap = mapOf(
            "S" to SECOND,
            "M" to MINUTE,
            "H" to HOUR,
            "D" to DAY,
            "W" to WEEK
        )

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

        fun encodeDelay(delayInMilliSeconds: Long): String {
            var result = ""
            var delay = delayInMilliSeconds
            while (delay > 0) {
                var max: Map.Entry<String, Long>? = null
                for (item in encodingMap) {
                    if (delay >= item.value)
                        max = item
                }
                if (max == null)
                    break
                result += (delay / max.value).toString() + max.key
                delay %= max.value
            }
            return result
        }

        fun decodeDelay(delay: String): Long {
            var result = 0L
            val pattern = Pattern.compile("([0-9]*)([SMHDW])")
            val matcher = pattern.matcher(delay)
            while (matcher.find()) {
                if (!encodingMap.containsKey(matcher.group(2) ?: ""))
                    return 0
                result += (matcher.group(1)?.toLong() ?: 0) * (encodingMap[matcher.group(2) ?: ""] ?: 0)
            }
            return result
        }
    }
}