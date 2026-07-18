package com.financeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.theme.Spacing
import java.time.LocalDateTime

data class TransactionFilter(
    val type: TransactionType? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    currentFilter: TransactionFilter?,
    onApply: (TransactionFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentFilter?.type) }
    var startDateText by remember { mutableStateOf(currentFilter?.startDate?.toLocalDate()?.toString() ?: "") }
    var endDateText by remember { mutableStateOf(currentFilter?.endDate?.toLocalDate()?.toString() ?: "") }
    var minAmountText by remember { mutableStateOf(currentFilter?.minAmount?.toString() ?: "") }
    var maxAmountText by remember { mutableStateOf(currentFilter?.maxAmount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Transaksi") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Transaction type filter
                Text("Tipe Transaksi")
                Spacer(modifier = Modifier.height(Spacing.sm))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("Semua") }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("Pemasukan") }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("Pengeluaran") }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Date range
                Text("Rentang Tanggal")
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Dari") },
                        placeholder = { Text("yyyy-MM-dd") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Sampai") },
                        placeholder = { Text("yyyy-MM-dd") },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Amount range
                Text("Rentang Jumlah")
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = { minAmountText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Min") },
                        placeholder = { Text("0") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = { maxAmountText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Max") },
                        placeholder = { Text("∞") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val filter = TransactionFilter(
                        type = selectedType,
                        startDate = startDateText.takeIf { it.isNotBlank() }?.let {
                            try { LocalDateTime.parse(it + "T00:00:00") } catch (_: Exception) { null }
                        },
                        endDate = endDateText.takeIf { it.isNotBlank() }?.let {
                            try { LocalDateTime.parse(it + "T23:59:59") } catch (_: Exception) { null }
                        },
                        minAmount = minAmountText.toDoubleOrNull(),
                        maxAmount = maxAmountText.toDoubleOrNull()
                    )
                    onApply(filter)
                }
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onApply(TransactionFilter())
                }
            ) {
                Text("Reset")
            }
        }
    )
}
