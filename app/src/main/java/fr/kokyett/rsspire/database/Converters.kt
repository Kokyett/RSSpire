package fr.kokyett.rsspire.database

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Converters {
    private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    @TypeConverter
    fun toNullableTimestamp(value: Date?): String? {
        return value?.let { dateFormat.format(it) }
    }

    @TypeConverter
    fun fromNullableTimestamp(value: String?): Date? {
        return value?.let { dateFormat.parse(it) }
    }
}