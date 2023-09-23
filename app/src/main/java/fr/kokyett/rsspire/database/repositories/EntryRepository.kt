package fr.kokyett.rsspire.database.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.room.Query
import fr.kokyett.rsspire.database.dao.EntryDao
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.database.entities.EntryIconsView
import fr.kokyett.rsspire.database.entities.EntryView
import kotlinx.coroutines.flow.Flow
import java.util.Date

class EntryRepository(private val entryDao: EntryDao) {
    fun get(id: Long): Entry {
        return entryDao.get(id)
    }

    fun getByCategory(id: Long?): LiveData<List<EntryView>> {
        return entryDao.getByCategory(id).asLiveData()
    }

    fun getExisting(idFeed: Long, guid: String?): Entry? {
        return entryDao.getExisting(idFeed, guid)
    }

    fun getIcons(id: Long): EntryIconsView? {
        return entryDao.getIcons(id)

    }

    fun markAsRead(id: Long) {
        entryDao.markAsRead(id, Date())
    }

    fun markAsUnread(id: Long) {
        entryDao.markAsUnread(id)
    }

    fun setFavorite(id: Long, isFavorite: Boolean) {
        entryDao.setFavorite(id, isFavorite)
    }


    fun save(entry: Entry) {
        if (entry.id == 0L)
            entry.id = entryDao.insert(entry)
        else
            entryDao.update(entry)
    }

    fun delete(entry: Entry) {
        entryDao.delete(entry)
    }
}
