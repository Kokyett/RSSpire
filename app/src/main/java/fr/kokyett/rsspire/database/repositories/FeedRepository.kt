package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.entities.Feed
import kotlinx.coroutines.flow.Flow

class FeedRepository(private val feedDao: FeedDao) {
    val allFeeds: LiveData<List<Feed>> = feedDao.getAll().asLiveData()

    @WorkerThread
    suspend fun get(id: Long): Feed? {
        return feedDao.get(id)
    }

    @WorkerThread
    suspend fun get(url: String): Feed? {
        return feedDao.get(url)
    }

    @WorkerThread
    suspend fun getByCategory(id: Long?): Flow<List<Feed>> {
        return feedDao.getByCategory(id)
    }

    @WorkerThread
    suspend fun urlExists(id: Long, url: String): Boolean {
        val feed = feedDao.get(url)
        return feed != null && feed.id != id
    }

    @WorkerThread
    suspend fun save(feed: Feed) {
        if (feed.id == 0L) feed.id = feedDao.insert(feed)
        else feedDao.update(feed)
    }

    @WorkerThread
    suspend fun delete(feed: Feed) {
        feedDao.delete(feed)
    }
}