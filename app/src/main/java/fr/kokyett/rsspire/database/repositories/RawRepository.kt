package fr.kokyett.rsspire.database.repositories

import androidx.sqlite.db.SimpleSQLiteQuery
import fr.kokyett.rsspire.database.dao.RawDao

class RawRepository(private val rawDao: RawDao) {
    fun vacuum() {
        rawDao.rawQuery(SimpleSQLiteQuery("vacuum"))
    }
}