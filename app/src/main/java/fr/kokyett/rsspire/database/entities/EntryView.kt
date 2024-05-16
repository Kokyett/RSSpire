package fr.kokyett.rsspire.database.entities

import androidx.room.Entity
import java.io.Serializable
import java.util.Date

@Entity
data class EntryView (
    var id: Long = 0,
    var idFeed: Long = 0,
    var guid: String?,
    var link: String?,
    var title: String?,
    var feedUrl: String,
    var feedTitle: String?,
    var isFavorite: Boolean,
    var publishDate: Date?,
    var readDate: Date?,
) : Serializable {
}
