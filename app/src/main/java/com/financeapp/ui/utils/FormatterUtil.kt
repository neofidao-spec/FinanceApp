package com.financeapp.ui.utils

import java.util.Locale

object FormatterUtil {
    private val indonesiaLocale = Locale("in", "ID")

    fun formatCurrency(amount: Double): String {
        return "Rp ${String.format(indonesiaLocale, "%,.0f", amount)}"
    }
}