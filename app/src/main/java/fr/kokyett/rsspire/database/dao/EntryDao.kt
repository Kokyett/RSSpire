package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("select * From Entry order by publishDate desc")
    fun getAll(): Flow<List<Entry>>

    @Insert
    fun insert(feed: Entry): Long

    @Update
    fun update(feed: Entry)

    @Delete
    fun delete(feed: Entry)
}