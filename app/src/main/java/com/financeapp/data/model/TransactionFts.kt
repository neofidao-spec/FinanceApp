package com.financeapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 virtual table for full-text search on transactions.
 * Mirrors the 'description' column of the transactions table.
 * Room keeps this in sync via triggers defined in the migration.
 */
@Fts4(contentEntity = Transaction::class)
@Entity(tableName = "transactions_fts")
data class TransactionFts(
    @ColumnInfo(name = "description")
    val description: String
)
