package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double = 0.0,
    val icon: String,
    val color: String,
    val isDefault: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class AccountType {
    CASH,
    BANK,
    EWALLET,
    CREDIT_CARD
}
