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
    @Query("select * from Entry order by publishDate desc")
    fun getAll(): Flow<List<Entry>>

    @Insert
    fun insert(entry: Entry): Long

    @Update
    fun update(entry: Entry)

    @Delete
    fun delete(entry: Entry)
}