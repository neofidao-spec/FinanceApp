package com.financeapp.data.database

import androidx.room.TypeConverter
import com.financeapp.data.model.AccountType
import com.financeapp.data.model.RecurringEndType
import com.financeapp.data.model.RecurringInterval
import com.financeapp.data.model.QuestCategory
import com.financeapp.data.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    // ── LocalDateTime ──────────────────────────────────────────
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }

    // ── LocalDate ──────────────────────────────────────────────
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }

    // ── TransactionType (Expense / Income) ─────────────────────
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE
        }
    }

    // ── AccountType (Cash / Bank / EWallet / Credit / Investment) ─
    @TypeConverter
    fun fromAccountType(value: AccountType): String {
        return value.name
    }

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return try {
            AccountType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AccountType.CASH
        }
    }

    // ── RecurringInterval (Daily / Weekly / Monthly / Yearly) ──
    @TypeConverter
    fun fromRecurringInterval(value: RecurringInterval): String {
        return value.name
    }

    @TypeConverter
    fun toRecurringInterval(value: String): RecurringInterval {
        return try {
            RecurringInterval.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RecurringInterval.MONTHLY
        }
    }

    // ── RecurringEndType (Never / AfterOccurrences / OnDate) ──
    @TypeConverter
    fun fromRecurringEndType(value: RecurringEndType): String {
        return value.name
    }

    @TypeConverter
    fun toRecurringEndType(value: String): RecurringEndType {
        return try {
            RecurringEndType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RecurringEndType.NEVER
        }
    }

    // ── QuestCategory (PENCATATAN / BUDGETING / EKSPLORASI / DISIPLIN / REVIEW) ─
    @TypeConverter
    fun fromQuestCategory(value: QuestCategory): String {
        return value.name
    }

    @TypeConverter
    fun toQuestCategory(value: String): QuestCategory {
        return try {
            QuestCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            QuestCategory.PENCATATAN
        }
    }
}
