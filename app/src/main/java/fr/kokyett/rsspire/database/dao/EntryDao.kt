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

    @Query("select e.id, e.idFeed, e.guid, e.link, e.title, f.url as feedUrl, f.title as feedTitle, e.isFavorite, e.publishDate, e.readDate from Entry e inner join Feed f on f.id = e.idFeed where idCategory = :id or (idCategory is null and :id is null) order by publishDate desc;")
    fun getByCategory(id: Long?): Flow<List<EntryView>>

    @Query("select * from Entry where idFeed = :idFeed and ((guid is null and :guid is null) or (guid = :guid)) order by publishDate desc")
    fun getExisting(idFeed: Long, guid: String?): Entry?

    @Query("select e.icon as entryIcon, f.icon as feedIcon from Entry e inner join Feed f on f.id = e.idFeed where e.id = :id")
    fun getIcons(id: Long): EntryIconsView?

    @Query("update Entry set readDate = :date where id = :id")
    fun markAsRead(id: Long, date: Date)

    @Query("update Entry set readDate = null where id = :id")
    fun markAsUnread(id: Long)

    @Insert
    fun insert(entry: Entry): Long

    @Update
    fun update(entry: Entry)

    @Delete
    fun delete(entry: Entry)
}
