package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.kokyett.rsspire.database.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("select * from Category order by name")
    fun getAll(): Flow<List<Category>>

    @Query("select * from Category where name = :name;")
    fun get(name: String): Category?

    @Insert
    fun insert(category: Category): Long

    @Update
    fun update(category: Category)

    @Delete
    fun delete(category: Category)
}