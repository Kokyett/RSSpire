package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.CategoryDao
import fr.kokyett.rsspire.database.entities.Category
import fr.kokyett.rsspire.database.entities.NullableCategory

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun get(id: Long): Category? {
        return categoryDao.get(id)
    }

    fun get(name: String): Category {
        var category = categoryDao.get(name)
        if (category == null) {
            category = Category(0, name)
            category.id = categoryDao.insert(category)
        }
        return category
    }

    fun getAll(): LiveData<List<Category>> {
        return categoryDao.getAll().asLiveData()
    }

    fun getWithFeeds(): LiveData<List<NullableCategory>> {
        return categoryDao.getWithFeeds().asLiveData()
    }

    fun save(category: Category) {
        if (category.id == 0L)
            category.id = categoryDao.insert(category)
        else
            categoryDao.update(category)
    }
}