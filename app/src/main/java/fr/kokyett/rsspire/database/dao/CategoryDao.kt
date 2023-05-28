package fr.kokyett.rsspire.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.kokyett.rsspire.database.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("select * from Category order by name;")
    fun getAll(): Flow<List<Category>>

    @Query("select * from Category where id = :id;")
    fun get(id: Long): Category?

    @Query("select * from Category where name = :name;")
    fun get(name: String): Category?

    @Insert
    suspend fun insert(category: Category): Long
}