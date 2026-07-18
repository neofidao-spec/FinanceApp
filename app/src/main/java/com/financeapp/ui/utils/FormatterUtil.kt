package com.financeapp.ui.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatterUtil {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val indonesiaLocale = Locale("in", "ID")

    fun formatCurrency(amount: Double): String {
        return "Rp ${String.format(indonesiaLocale, "%,.0f", amount)}"
    }

    fun formatDate(date: LocalDateTime): String {
        return date.format(dateFormatter)
    }

    fun formatDateTime(date: LocalDateTime): String {
        return date.format(dateTimeFormatter)
    }

    fun parseAmount(amount: String): Double? {
        return amount.toDoubleOrNull()
    }
}
