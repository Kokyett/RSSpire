package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.entities.Feed
import kotlinx.coroutines.flow.Flow

class FeedRepository(private val feedDao: FeedDao) {
    fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll().asLiveData()
    }

    fun getLogsFeed(): Feed {
        return feedDao.getLogsFeed()
    }

    @WorkerThread
    fun getForUrl(url: String): Feed? {
        return feedDao.getForUrl(url)
    }

    fun getExportByCategory(id: Long?): Flow<List<Feed>> {
        return feedDao.getExportByCategory(id)
    }

    @WorkerThread
    fun save(feed: Feed) {
        if (feed.id == 0L)
            feed.id = feedDao.insert(feed)
        else
            feedDao.update(feed)
    }

    @WorkerThread
    fun delete(feed: Feed) {
        feedDao.delete(feed)
    }
}