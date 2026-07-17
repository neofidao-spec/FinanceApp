package com.financeapp.util

import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun exportTransactions(transactions: List<TransactionWithCategory>): String {
        val sb = StringBuilder()
        sb.appendLine("Date;Type;Category;Description;Amount")

        for (twc in transactions) {
            val date = twc.transaction.date.format(dateFormatter)
            val type = if (twc.transaction.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran"
            val category = twc.category.name
            val description = twc.transaction.description.replace(";", ",")
            val amount = String.format("%.0f", twc.transaction.amount)

            sb.appendLine("$date;$type;$category;$description;$amount")
        }

        return sb.toString()
    }
}
