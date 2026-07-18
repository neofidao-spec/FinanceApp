package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Query
import com.financeapp.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

/**
 * DAO for FTS4 full-text search on transactions.
 * Searches the FTS virtual table, then joins back to the real transactions table.
 */
@Dao
interface TransactionFtsDao {
    /**
     * Full-text search on transaction descriptions via FTS4.
     * Returns matching transactions with their categories, ordered by date descending.
     */
    @androidx.room.Transaction
    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN transactions_fts fts ON t.id = fts.rowid
        WHERE transactions_fts MATCH :query
        ORDER BY t.date DESC
    """)
    fun search(query: String): Flow<List<TransactionWithCategory>>
}
