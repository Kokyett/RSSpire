package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.Category
import fr.kokyett.rsspire.database.entities.NullableCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("select * from Category where id = :id;")
    fun get(id: Long): Category?

    @Query("select * from Category where name = :name;")
    fun get(name: String): Category?

    @Query("select * from Category order by name")
    fun getAll(): Flow<List<Category>>

    @Query("select distinct c.id, c.name from Feed f left join Category c on c.id = f.idCategory order by name")
    fun getWithFeeds(): Flow<List<NullableCategory>>

    @Query("select distinct c.id, c.name from Entry e inner join Feed f on f.id  = e.idFeed left join Category c on c.id = f.idCategory order by name")
    fun getWithEntries(): Flow<List<NullableCategory>>

    @Query("select distinct c.id, c.name"+
            " from Entry e" +
            " inner join Feed f on f.id  = e.idFeed" +
            " left join Category c on c.id = f.idCategory" +
            " where e.isFavorite = 1 or e.readDate is null" +
            " order by name")
    fun getWithUnreadEntries(): Flow<List<NullableCategory>>

    @Insert
    fun insert(category: Category): Long

    @Update
    fun update(category: Category)

    @Delete
    fun delete(category: Category)
}