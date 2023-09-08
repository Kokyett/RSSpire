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
    @Query("select * from Feed where id = :id;")
    fun get(id: Long): Feed?

    @Query("select * from Feed order by title, url")
    fun getAll(): Flow<List<Feed>>

    @Query("select * from Feed where type = 'LOG'")
    fun getLogsFeed(): Feed

    @Query("select * from Feed where type <> 'LOG' and (idCategory = :id or (idCategory is null and :id is null)) order by title, url;")
    fun getExportByCategory(id: Long?): Flow<List<Feed>>

    @Query("select * from Feed where idCategory = :id or (idCategory is null and :id is null) order by title, url;")
    fun getByCategory(id: Long?): Flow<List<Feed>>

    @Query("select * from Feed where url = :url;")
    fun getByUrl(url: String): Feed?

    @Insert
    fun insert(feed: Feed): Long

    @Update
    fun update(feed: Feed)

    @Delete
    fun delete(feed: Feed)
}