package fr.kokyett.rsspire.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.enums.FeedType
import fr.kokyett.rsspire.utils.DateTime
import java.io.Serializable
import java.util.Date

@Entity(
    indices = [Index(value = ["url"], unique = true), Index(value = ["idCategory"])],
    foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["idCategory"], onDelete = ForeignKey.SET_NULL)]
)
data class Feed(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var idCategory: Long? = null,
    var type: FeedType = FeedType.RSS,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var url: String = "",
    @ColumnInfo(collate = ColumnInfo.NOCASE) var title: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) var description: String? = null,
    var lastEntryDate: Date? = null,
    var nextRefreshDate: Date? = null,
    var refreshInterval: Long = DateTime.decodeDelay(ApplicationContext.getStringPreference("pref_default_refresh_interval", "2D")!!),
    var deleteReadEntriesInterval: Long = DateTime.decodeDelay(
        ApplicationContext.getStringPreference(
            "pref_default_delete_read_entries_interval",
            "1W"
        )!!
    ),
    var downloadFullContent: Boolean = false,
    var replaceThumbnails: Boolean = false,
    var icon: ByteArray? = null,
    var iconUrl: String? = null,
) : Serializable {
}