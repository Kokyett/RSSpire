package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.FeedDao
import fr.kokyett.rsspire.database.entities.Feed

class FeedRepository(private val feedDao: FeedDao) {
    @WorkerThread
    fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll().asLiveData()
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