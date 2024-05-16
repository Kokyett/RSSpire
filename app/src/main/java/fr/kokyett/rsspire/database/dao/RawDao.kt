package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface RawDao {
    @RawQuery
    fun rawQuery(supportSQLiteQuery: SupportSQLiteQuery): Int
}