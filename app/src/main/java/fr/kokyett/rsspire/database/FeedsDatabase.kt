package fr.kokyett.rsspire.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.kokyett.rsspire.database.dao.CategoryDao
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.entities.Category
import fr.kokyett.rsspire.database.entities.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Feed::class], version = 1, exportSchema = false)
abstract class FeedsDatabase : RoomDatabase() {
    abstract val categoryDao: CategoryDao
    abstract val feedDao: FeedDao

    companion object {
        @Volatile
        private var INSTANCE: FeedsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FeedsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(context.applicationContext, FeedsDatabase::class.java, "RSSpire.db")
                    .addCallback(FeedsDatabaseCallback(scope, context))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class FeedsDatabaseCallback(private val scope: CoroutineScope, private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: FeedsDatabase) {
            //TODO: init databasse
        }
    }
}