package fr.kokyett.rsspire.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.kokyett.rsspire.enums.FeedType
import java.io.Serializable

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
    var icon: ByteArray? = null,
    var iconUrl: String? = null,
) : Serializable {
}