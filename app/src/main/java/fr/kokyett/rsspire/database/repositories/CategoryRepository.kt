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

    @WorkerThread
    fun save(category: Category) {
        if (category.id == 0L)
            category.id = categoryDao.insert(category)
        else
            categoryDao.update(category)
    }

    @WorkerThread
    fun delete(category: Category) {
        categoryDao.delete(category)
    }
}