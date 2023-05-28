package fr.kokyett.rsspire

import android.app.Application
import fr.kokyett.rsspire.database.FeedsDatabase
import fr.kokyett.rsspire.database.repositories.CategoryRepository
import fr.kokyett.rsspire.database.repositories.FeedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RSSpireApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { FeedsDatabase.getDatabase(this, applicationScope) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao) }
    val feedRepository by lazy { FeedRepository(database.feedDao) }
}