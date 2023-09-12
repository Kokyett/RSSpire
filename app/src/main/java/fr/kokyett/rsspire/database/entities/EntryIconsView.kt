package fr.kokyett.rsspire.database.entities

import androidx.room.Entity

@Entity
data class EntryIconsView(
    var entryIcon: ByteArray? = null,
    var feedIcon: ByteArray? = null
){

}