package fr.kokyett.rsspire.database.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import fr.kokyett.rsspire.database.dao.CategoryDao
import fr.kokyett.rsspire.database.dao.EntryDao
import fr.kokyett.rsspire.database.entities.EntryView
import fr.kokyett.rsspire.database.entities.NullableCategory

class PreferencesRepository(private val categoryDao: CategoryDao, private val entryDao: EntryDao) {
    private val onlyDisplayUnreadEntries: MutableLiveData<Boolean> = MutableLiveData()

    fun getCategories(): LiveData<List<NullableCategory>> {
        return onlyDisplayUnreadEntries.switchMap {
            if (it)
                categoryDao.getWithUnreadEntries().asLiveData()
            else
                categoryDao.getWithEntries().asLiveData()
        }
    }

    fun getEntriesByCategory(idCategory: Long?): LiveData<List<EntryView>> {
        return onlyDisplayUnreadEntries.switchMap {
            if (it)
                entryDao.getUnreadByCategory(idCategory).asLiveData()
            else
                entryDao.getByCategory(idCategory).asLiveData()
        }
    }

    fun getEntriesByFeed(idFeed: Long?): LiveData<List<EntryView>> {
        return onlyDisplayUnreadEntries.switchMap {
            if (it)
                entryDao.getUnreadByFeed(idFeed).asLiveData()
            else
                entryDao.getByFeed(idFeed).asLiveData()
        }
    }

    fun setOnlyDisplayUnreadEntries(value: Boolean) {
        onlyDisplayUnreadEntries.value = value
    }
}