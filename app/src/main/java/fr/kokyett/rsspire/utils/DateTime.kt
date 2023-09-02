package fr.kokyett.rsspire.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTime {
    companion object {
        fun now(format: String): String {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
        }
    }
}