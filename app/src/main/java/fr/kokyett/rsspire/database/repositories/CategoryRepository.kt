package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.CategoryDao
import fr.kokyett.rsspire.database.entities.Category


class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAll(): LiveData<List<Category>> {
        return categoryDao.getAll().asLiveData()
    }

    fun get(name: String): Category {
        var category = categoryDao.get(name)
        if (category == null) {
            category = Category(0, name)
            category.id = categoryDao.insert(category)
        }
        return category
    }

    @WorkerThread
    fun save(category: Category) {
        if (category.id == 0L)
            category.id = categoryDao.insert(category)
        else
            categoryDao.update(category)
    }
}