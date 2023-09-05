package fr.kokyett.rsspire.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class NullableCategory(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var name: String? = null
)