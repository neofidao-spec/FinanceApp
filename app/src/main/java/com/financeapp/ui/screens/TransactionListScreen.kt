package com.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.ui.components.FilterDialog
import com.financeapp.ui.components.SearchBar
import com.financeapp.ui.components.SwipeableTransactionItem
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onTransactionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show undo snackbar when a transaction is deleted via swipe
    LaunchedEffect(uiState.deletedTransactionId) {
        uiState.deletedTransactionId?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Transaksi dihapus",
                actionLabel = "Urungkan",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                onFilterClick = { viewModel.showFilterDialog() },
                modifier = Modifier.padding(top = 8.dp)
            )

            // Active filter summary chips
            if (uiState.activeFilter != null) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.activeFilter?.type?.let { type ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilter() },
                            label = {
                                Text(
                                    if (type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran"
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Hapus filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    uiState.activeFilter?.startDate?.let {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilter() },
                            label = { Text("Dari: ${it.toLocalDate()}") }
                        )
                    }
                    uiState.activeFilter?.endDate?.let {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilter() },
                            label = { Text("Sampai: ${it.toLocalDate()}") }
                        )
                    }
                    uiState.activeFilter?.minAmount?.let {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilter() },
                            label = { Text("Min: ${FormatterUtil.formatCurrency(it)}") }
                        )
                    }
                    uiState.activeFilter?.maxAmount?.let {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilter() },
                            label = { Text("Max: ${FormatterUtil.formatCurrency(it)}") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
                EmptyTransactionState(
                    isFiltered = uiState.searchQuery.isNotBlank() || uiState.activeFilter != null
                )
            } else {
                val groupedTransactions = groupTransactionsByDate(uiState.filteredTransactions.take(uiState.visibleCount))
                val listState = rememberLazyListState()

                // Infinite scroll: load more when reaching the last visible item
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val totalItems = listState.layoutInfo.totalItemsCount
                        lastVisibleItem >= totalItems - 3 && uiState.hasMore && !uiState.isLoadingMore
                    }
                }
                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore) viewModel.loadMore()
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedTransactions.forEach { (dateLabel, transactions) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                        items(transactions) { txn ->
                            SwipeableTransactionItem(
                                transaction = txn,
                                onSwipeEdit = { onTransactionClick(txn.transaction.id) },
                                onSwipeDelete = { viewModel.swipeDeleteTransaction(txn) },
                                onClick = { onTransactionClick(txn.transaction.id) }
                            )
                        }
                        item {
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }

        // Snackbar host for undo
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // Filter Dialog — driven by ViewModel state
    if (uiState.showFilterDialog) {
        FilterDialog(
            currentFilter = uiState.activeFilter,
            onApply = { filter ->
                viewModel.applyFilter(filter)
                viewModel.hideFilterDialog()
            },
            onDismiss = { viewModel.hideFilterDialog() }
        )
    }
}

@Composable
private fun EmptyTransactionState(
    isFiltered: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Tidak ada transaksi",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isFiltered) "Tidak ada transaksi ditemukan" else "Belum ada transaksi",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isFiltered) "Coba ubah filter atau kata kunci pencarian"
                else "Mulai dengan menambahkan transaksi baru",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun groupTransactionsByDate(
    transactions: List<TransactionWithCategory>
): Map<String, List<TransactionWithCategory>> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return transactions
        .groupBy { it.transaction.date.toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .mapKeys { (date, _) ->
            when {
                date == today -> "Hari Ini"
                date == yesterday -> "Kemarin"
                else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            }
        }
}
