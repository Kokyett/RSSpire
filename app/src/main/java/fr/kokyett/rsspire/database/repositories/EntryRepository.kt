package fr.kokyett.rsspire.database.repositories

import fr.kokyett.rsspire.database.dao.EntryDao
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.database.entities.EntryIconsView

class EntryRepository(private val entryDao: EntryDao) {
    fun get(id: Long): Entry {
        return entryDao.get(id)
    }

    fun getExisting(idFeed: Long, guid: String?): Entry? {
        return entryDao.getExisting(idFeed, guid)
    }

    fun getIcons(id: Long): EntryIconsView? {
        return entryDao.getIcons(id)

    }

    fun markAsRead(id: Long) {
        entryDao.markAsRead(id)
    }

    fun markAsUnread(id: Long) {
        entryDao.markAsUnread(id)
    }

    fun markAllAsReadByCategory(idCategory: Long?) {
        entryDao.markAllAsReadByCategory(idCategory)
    }

    fun markAllAsReadByFeed(idFeed: Long?) {
        entryDao.markAllAsReadByFeed(idFeed)
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

    fun deleteFeedEntries(idFeed: Long) {
        entryDao.deleteFeedEntries(idFeed)
    }

    fun deleteReadEntries() {
        entryDao.deleteReadEntries()
    }
}
