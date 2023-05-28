package fr.kokyett.rsspire.database.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import fr.kokyett.rsspire.database.dao.CategoryDao
import fr.kokyett.rsspire.database.entities.Category

class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: LiveData<List<Category>> = categoryDao.getAll().asLiveData()

    @WorkerThread
    suspend fun get(id: Long): Category? {
        return categoryDao.get(id)
    }

    @WorkerThread
    suspend fun get(name: String): Category {
        var category = categoryDao.get(name)
        if (category == null) {
            category = Category(0, name)
            category.id = categoryDao.insert(category)
        }
        return category
    }
}