package fr.kokyett.rsspire.database.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.entities.Feed
import fr.kokyett.rsspire.database.entities.FeedInformation
import kotlinx.coroutines.flow.Flow

class FeedRepository(private val feedDao: FeedDao) {
    fun get(id: Long): Feed? {
        return feedDao.get(id)
    }

    fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll().asLiveData()
    }

    fun getRefresh(): List<Feed> {
        return feedDao.getRefresh()
    }

    fun getLogsFeed(): Feed {
        return feedDao.getLogsFeed()
    }

    fun getForUrl(url: String): Feed? {
        return feedDao.getByUrl(url)
    }

    fun getByCategory(id: Long?): LiveData<List<FeedInformation>> {
        return feedDao.getByCategory(id).asLiveData()
    }

    fun markAllAsRead(idFeed: Long?) {
        return feedDao.markAllAsRead(idFeed)
    }

    fun markAllAsUnread(idFeed: Long?) {
        return feedDao.markAllAsUnread(idFeed)
    }

    fun getExportByCategory(id: Long?): Flow<List<Feed>> {
        return feedDao.getExportByCategory(id)
    }

    fun urlExists(id: Long, url: String): Boolean {
        val feed = feedDao.getByUrl(url)
        return feed != null && feed.id != id
    }

    fun updateIcon(id: Long, icon: ByteArray) {
        feedDao.updateIcon(id, icon)
    }

    fun reinitialize(id: Long) {
        feedDao.reinitialize(id)
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