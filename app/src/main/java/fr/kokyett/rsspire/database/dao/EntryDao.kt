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

    @Query("select * from Entry where id = :id")
    fun get(id: Long): Flow<Entry>

    @Query("select * from Entry order by publishDate desc")
    fun getAll(): Flow<List<Entry>>

    @Query("select e.* from Entry e inner join Feed f on f.id = e.idFeed where idCategory = :id or (idCategory is null and :id is null) order by publishDate desc;")
    fun getByCategory(id: Long?): Flow<List<Entry>>

    @Insert
    fun insert(entry: Entry): Long

    @Update
    fun update(entry: Entry)

    @Delete
    fun delete(entry: Entry)
}