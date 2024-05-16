package fr.kokyett.rsspire.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(
    indices = [
        Index(value = ["idFeed"]),
        Index(value = ["guid"])
    ],
    foreignKeys = [ForeignKey(entity = Feed::class, parentColumns = ["id"], childColumns = ["idFeed"], onDelete = ForeignKey.CASCADE)]
)
data class Entry(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var idFeed: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var guid: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var link: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var title: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var author: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var content: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var categories: String? = null,
    var isFavorite: Boolean = false,
    var publishDate: Date? = null,
    var readDate: Date? = null
) : Serializable {
}