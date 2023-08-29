package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.EntryDao
import fr.kokyett.rsspire.database.entities.Entry

class EntryRepository(private val entryDao: EntryDao) {
    fun getAll(): LiveData<List<Entry>> {
        return entryDao.getAll().asLiveData()
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