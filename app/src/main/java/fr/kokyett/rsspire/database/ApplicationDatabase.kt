package fr.kokyett.rsspire.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.database.dao.CategoryDao
import fr.kokyett.rsspire.database.dao.EntryDao
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.dao.RawDao
import fr.kokyett.rsspire.database.entities.Category
import fr.kokyett.rsspire.database.entities.Entry
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.enums.FeedType
import fr.kokyett.rsspire.utils.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Feed::class, Entry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract val categoryDao: CategoryDao
    abstract val feedDao: FeedDao
    abstract val entryDao: EntryDao
    abstract val rawDao: RawDao

    companion object {
        @Volatile
        private var handle: ApplicationDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ApplicationDatabase {
            return handle ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, ApplicationDatabase::class.java, "RSSpire.db")
                    .addCallback(ApplicationDatabaseDatabaseCallback(scope)).build()
                handle = instance
                return instance
            }
        }
    }

    private class ApplicationDatabaseDatabaseCallback(private val scope: CoroutineScope) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            handle?.let { database ->
                scope.launch {
                    populateDatabase(database)
                }
            }
        }

        fun populateDatabase(database: ApplicationDatabase) {
            val category = Category(name = ApplicationContext.getString(R.string.database_initial_category))
            category.id = database.categoryDao.insert(category)

            var feed = Feed(
                idCategory = category.id,
                type = FeedType.LOG,
                url = ApplicationContext.getString(R.string.database_initial_feed_logs),
                title = ApplicationContext.getString(R.string.database_initial_feed_logs),
                iconUrl = "https://raw.githubusercontent.com/Kokyett/RSSpire/main/logo.png",
                refreshInterval = DateTime.DAY
            )
            feed.id = database.feedDao.insert(feed)

            feed = Feed(
                idCategory = category.id,
                url = "https://github.com/Kokyett.atom",
                title = "Kokyett GitHub public timeline",
                iconUrl = "https://avatars.githubusercontent.com/u/80988927?v=4"
            )
            feed.id = database.feedDao.insert(feed)

            feed = Feed(
                idCategory = category.id,
                url = "https://github.com/Kokyett/RSSpire/releases.atom",
                title = "Release notes from RSSpire",
                iconUrl = "https://raw.githubusercontent.com/Kokyett/RSSpire/main/logo.png"
            )
            feed.id = database.feedDao.insert(feed)
        }
    }
}