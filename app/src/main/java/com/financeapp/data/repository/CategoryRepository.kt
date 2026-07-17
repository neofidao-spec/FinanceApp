package com.financeapp.data.repository

import com.financeapp.data.database.CategoryDao
import com.financeapp.data.model.Category
import com.financeapp.data.model.DefaultCategories
import com.financeapp.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    fun getAllCategories(): Flow<List<Category>> = dao.getAll()

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> = dao.getByType(type)

    suspend fun addCategory(category: Category): Long = dao.insert(category)

    suspend fun updateCategory(category: Category) = dao.update(category)

    suspend fun deleteCategory(category: Category) = dao.delete(category)

    suspend fun getCategory(id: Long): Category? = dao.getById(id)

    suspend fun initializeDefaultCategories() {
        if (dao.count() == 0) {
            dao.insertAll(DefaultCategories.getDefault())
        }
    }
}
