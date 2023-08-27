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
        Index(value = ["idFeed"])
    ],
    foreignKeys = [ForeignKey(entity = Feed::class, parentColumns = ["id"], childColumns = ["idFeed"], onDelete = ForeignKey.CASCADE)]
)
data class Entry(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var idFeed: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var title: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var content: String? = null,
    var publishDate: Date? = null,
    var readDate: Date? = null,
    var icon: ByteArray? = null
) : Serializable {
}