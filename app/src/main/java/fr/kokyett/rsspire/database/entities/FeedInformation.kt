package fr.kokyett.rsspire.database.entities

import androidx.room.Entity
import fr.kokyett.rsspire.enums.FeedType
import java.io.Serializable
import java.util.Date

@Entity
data class FeedInformation(
    var id: Long = 0,
    var type: FeedType = FeedType.RSS,
    var url: String = "",
    var title: String? = null,
    var description: String? = null,
    var lastEntryDate: Date? = null,
    var icon: ByteArray? = null,
    var iconUrl: String? = null,
    var unread: Int,
    var entries: Int,
) : Serializable {

}