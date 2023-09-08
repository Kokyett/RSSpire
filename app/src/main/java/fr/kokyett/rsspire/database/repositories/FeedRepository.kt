package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.entities.Feed
import kotlinx.coroutines.flow.Flow

class FeedRepository(private val feedDao: FeedDao) {
    fun get(id: Long): Feed? {
        return feedDao.get(id)
    }

    fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll().asLiveData()
    }

    fun getLogsFeed(): Feed {
        return feedDao.getLogsFeed()
    }

    fun getForUrl(url: String): Feed? {
        return feedDao.getByUrl(url)
    }

    fun getByCategory(id: Long?): LiveData<List<Feed>> {
        return feedDao.getByCategory(id).asLiveData()
    }

    fun getExportByCategory(id: Long?): Flow<List<Feed>> {
        return feedDao.getExportByCategory(id)
    }

    fun urlExists(id: Long, url: String): Boolean {
        val feed = feedDao.getByUrl(url)
        return feed != null && feed.id != id
    }

    fun save(feed: Feed) {
        if (feed.id == 0L)
            feed.id = feedDao.insert(feed)
        else
            feedDao.update(feed)
    }

    fun delete(feed: Feed) {
        feedDao.delete(feed)
    }
}