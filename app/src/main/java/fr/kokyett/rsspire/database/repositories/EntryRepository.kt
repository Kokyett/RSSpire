package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.EntryDao
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.database.entities.Feed

class EntryRepository(private val entryDao: EntryDao) {
    fun get(id: Long): LiveData<Entry> {
        return entryDao.get(id).asLiveData()
    }

    fun getByCategory(id: Long?): LiveData<List<Entry>> {
        return entryDao.getByCategory(id).asLiveData()
    }

    @WorkerThread
    fun save(entry: Entry) {
        if (entry.id == 0L)
            entry.id = entryDao.insert(entry)
        else
            entryDao.update(entry)
    }

    @WorkerThread
    fun delete(entry: Entry) {
        entryDao.delete(entry)
    }
}