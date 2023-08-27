package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.Feed
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("select * From Feed order by title, url")
    fun getAll(): Flow<List<Feed>>

    @Insert
    fun insert(feed: Feed): Long

    @Update
    fun update(feed: Feed)

    @Delete
    fun delete(feed: Feed)
}