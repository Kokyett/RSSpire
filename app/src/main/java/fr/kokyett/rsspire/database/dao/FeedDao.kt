package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.CategoryWithFeeds
import fr.kokyett.rsspire.database.entities.Feed
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("select * from Feed order by title, url;")
    fun getAll(): Flow<List<Feed>>

    @Query("select * from Feed where idCategory = :id or (idCategory is null and :id is null) order by title, url;")
    fun getByCategory(id: Long?): Flow<List<Feed>>

    @Query("select distinct c.* from Feed f left join Category c on c.id = f.idCategory order by c.name;")
    fun getCategoriesWithFeeds(): Flow<List<CategoryWithFeeds>>

    @Query("select * from Feed where id = :id;")
    fun get(id: Long): Feed?

    @Query("select * from Feed where url = :url;")
    fun get(url: String): Feed?

    @Insert
    suspend fun insert(feed: Feed): Long

    @Update
    suspend fun update(feed: Feed)

    @Delete
    suspend fun delete(feed: Feed)
}