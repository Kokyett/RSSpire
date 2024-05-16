package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.database.entities.FeedInformation
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FeedDao {
    @Query("select * from Feed where id = :id;")
    fun get(id: Long): Feed?

    @Query("select * from Feed order by title, url")
    fun getAll(): Flow<List<Feed>>

    @Query("select * from Feed where nextRefreshDate is null or nextRefreshDate <= :date")
    fun getRefresh(date: Date = Date()): List<Feed>

    @Query("select * from Feed where type = 'LOG'")
    fun getLogsFeed(): Feed

    @Query("select * from Feed where type <> 'LOG' and (idCategory = :id or (idCategory is null and :id is null)) order by title, url;")
    fun getExportByCategory(id: Long?): Flow<List<Feed>>

    @Query("select" +
            " f.id," +
            " f.url," +
            " f.title," +
            " f.description," +
            " f.type," +
            " f.icon," +
            " f.iconUrl," +
            " f.lastEntryDate," +
            " sum(case when e.id is not null and e.readDate is null then 1 else 0 end) as unread," +
            " count(e.id) as entries" +
            " from Feed f" +
            " left join Entry e on e.idFeed = f.id" +
            " where f.idCategory = :id or (f.idCategory is null and :id is null)" +
            " group by f.id, f.url, f.title, f.description, f.type, f.icon, f.lastEntryDate" +
            " order by f.title, f.url;")
    fun getByCategory(id: Long?): Flow<List<FeedInformation>>

    @Query("update Entry set readDate = :date where id in (" +
            "select id" +
            " from Entry" +
            " where readDate is null and idFeed = :idFeed)")
    fun markAllAsRead(idFeed: Long?, date: Date = Date())

    @Query("update Entry set readDate = null where id in (" +
            "select id" +
            " from Entry" +
            " where idFeed = :idFeed)")
    fun markAllAsUnread(idFeed: Long?)

    @Query("select * from Feed where url = :url;")
    fun getByUrl(url: String): Feed?

    @Query("update Feed set icon = :icon where id = :id")
    fun updateIcon(id: Long, icon: ByteArray)

    @Query("update Feed set lastEntryDate = null, nextRefreshDate = null where id = :id")
    fun reinitialize(id: Long)

    @Insert
    fun insert(feed: Feed): Long

    @Update
    fun update(feed: Feed)

    @Delete
    fun delete(feed: Feed)
}