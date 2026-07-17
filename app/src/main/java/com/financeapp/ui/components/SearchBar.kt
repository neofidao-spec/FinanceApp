package com.financeapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SearchBar(
    query: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Cari transaksi..."
) {
    var localQuery by remember { mutableStateOf(query) }

    // Sync local state if external query changes
    LaunchedEffect(query) {
        if (query != localQuery) {
            localQuery = query
        }
    }

    // Debounced search: emit after 300ms delay
    LaunchedEffect(localQuery) {
        delay(300)
        onSearchChange(localQuery)
    }

    OutlinedTextField(
        value = localQuery,
        onValueChange = { localQuery = it },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            Row {
                if (localQuery.isNotEmpty()) {
                    IconButton(onClick = { localQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Filter"
                    )
                }
            }
        },
        singleLine = true
    )
}
