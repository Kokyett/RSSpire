package fr.kokyett.rsspire.database.entities

import androidx.room.Entity

@Entity
data class EntryIconsView(
    var feedIcon: ByteArray? = null
){

}