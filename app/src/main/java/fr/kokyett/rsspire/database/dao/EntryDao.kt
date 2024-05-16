package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.database.entities.EntryIconsView
import fr.kokyett.rsspire.database.entities.EntryView
import kotlinx.coroutines.flow.Flow
import java.util.Date


@Dao
interface EntryDao {

    @Query("select * from Entry where id = :id")
    fun get(id: Long): Entry

    @Query("select * from Entry order by publishDate desc")
    fun getAll(): Flow<List<Entry>>

    @Query(
        "select e.id, e.idFeed, e.guid, e.link, e.title, f.url as feedUrl, f.title as feedTitle, e.isFavorite, e.publishDate, e.readDate" +
                " from Entry e" +
                " inner join Feed f on f.id = e.idFeed" +
                " where idCategory = :id or (idCategory is null and :id is null)" +
                " order by publishDate desc;"
    )
    fun getByCategory(id: Long?): Flow<List<EntryView>>

    @Query(
        "select e.id, e.idFeed, e.guid, e.link, e.title, f.url as feedUrl, f.title as feedTitle, e.isFavorite, e.publishDate, e.readDate" +
                " from Entry e" +
                " inner join Feed f on f.id = e.idFeed" +
                " where (idCategory = :id or (idCategory is null and :id is null)) and (isFavorite = 1 or readDate is null)" +
                " order by publishDate desc;"
    )
    fun getUnreadByCategory(id: Long?): Flow<List<EntryView>>

    @Query(
        "select e.id, e.idFeed, e.guid, e.link, e.title, f.url as feedUrl, f.title as feedTitle, e.isFavorite, e.publishDate, e.readDate" +
                " from Entry e" +
                " inner join Feed f on f.id = e.idFeed" +
                " where f.id = :id" +
                " order by publishDate desc;"
    )
    fun getByFeed(id: Long?): Flow<List<EntryView>>

    @Query(
        "select e.id, e.idFeed, e.guid, e.link, e.title, f.url as feedUrl, f.title as feedTitle, e.isFavorite, e.publishDate, e.readDate" +
                " from Entry e" +
                " inner join Feed f on f.id = e.idFeed" +
                " where f.id = :id and (isFavorite = 1 or readDate is null)" +
                " order by publishDate desc;"
    )
    fun getUnreadByFeed(id: Long?): Flow<List<EntryView>>

    @Query("select * from Entry where idFeed = :idFeed and ((guid is null and :guid is null) or (guid = :guid)) order by publishDate desc")
    fun getExisting(idFeed: Long, guid: String?): Entry?

    @Query("select f.icon as feedIcon from Entry e inner join Feed f on f.id = e.idFeed where e.id = :id")
    fun getIcons(id: Long): EntryIconsView?

    @Insert
    fun insert(entry: Entry): Long

    @Update
    fun update(entry: Entry)

    @Query("update Entry set readDate = :date where id = :id")
    fun markAsRead(id: Long, date: Date = Date())

    @Query("update Entry set readDate = :date where id in (" +
            "select e.id" +
            " from Entry e" +
            " inner join Feed f on f.id = e.idFeed" +
            " left join Category c on c.id = f.idCategory" +
            " where readDate is null and ((idCategory is null and :idCategory is null) or idCategory = :idCategory))")
    fun markAllAsReadByCategory(idCategory: Long?, date: Date = Date())

    @Query("update Entry set readDate = :date where id in (" +
            "select id" +
            " from Entry" +
            " where readDate is null and idFeed = :idFeed)")
    fun markAllAsReadByFeed(idFeed: Long?, date: Date = Date())

    @Query("update Entry set readDate = null where id in (" +
            "select id" +
            " from Entry" +
            " where readDate is not null and idFeed = :idFeed)")
    fun markAllAsUnreadByFeed(idFeed: Long?)

    @Query("update Entry set readDate = null where id = :id")
    fun markAsUnread(id: Long)

    @Query("update Entry set isFavorite = :isFavorite where id = :id")
    fun setFavorite(id: Long, isFavorite: Boolean)

    @Delete
    fun delete(entry: Entry)

    @Query("delete from Entry where id in" +
            "(select id" +
            " from Entry " +
            " where isFavorite = 0" +
            " and readDate is not null" +
            " and idFeed = :idFeed)")
    fun deleteFeedEntries(idFeed: Long)

    @Query("delete from Entry where id in" +
            "(select e.id" +
            " from Entry e" +
            " inner join Feed f on f.id = e.idFeed" +
            " where isFavorite = 0" +
            " and readDate is not null" +
            " and f.deleteReadEntriesInterval <> 0" +
            " and (f.deleteReadEntriesInterval + e.readDate) < :date" +
            " and e.publishDate < :date)")
    fun deleteReadEntries(date: Date = Date())
}
