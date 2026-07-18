package com.financeapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeapp.data.model.Account
import com.financeapp.data.model.AccountType
import com.financeapp.ui.theme.Spacing

/**
 * Standalone AccountSelector component.
 * Dropdown style — shows selected account with icon/color, expands to pick another.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(
    accounts: List<Account>,
    selectedAccountId: Long,
    onAccountSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedAccountId }

    Column(modifier = modifier) {
        Text(
            text = "Akun",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedAccount?.let { "${it.name}" } ?: "Pilih akun",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = {
                    AccountIcon(
                        type = selectedAccount?.type ?: AccountType.CASH,
                        color = selectedAccount?.color?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary,
                        size = 24
                    )
                },
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.smd)
                            ) {
                                AccountIcon(
                                    type = account.type,
                                    color = parseColor(account.color),
                                    size = 24
                                )
                                Column {
                                    Text(
                                        text = account.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (account.id == selectedAccountId) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "Rp${String.format("%,.0f", account.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (account.id == selectedAccountId) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Terpilih",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onAccountSelected(account.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Chip-style AccountSelectorRow — compact horizontal chips.
 */
@Composable
fun AccountSelectorRow(
    accounts: List<Account>,
    selectedAccountId: Long,
    onAccountSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Akun",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            accounts.forEach { account ->
                val isSelected = account.id == selectedAccountId
                OutlinedCard(
                    onClick = { onAccountSelected(account.id) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(
                            2.dp,
                            parseColor(account.color)
                        )
                    } else {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AccountIcon(
                            type = account.type,
                            color = parseColor(account.color),
                            size = 28
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountIcon(
    type: AccountType,
    color: Color,
    size: Int
) {
    val icon: ImageVector = when (type) {
        AccountType.CASH -> Icons.Filled.AttachMoney
        AccountType.BANK -> Icons.Filled.AccountBalance
        AccountType.EWALLET -> Icons.Filled.AccountBalanceWallet
        AccountType.CREDIT_CARD -> Icons.Filled.CreditCard
    }
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(
                if (color != Color.Transparent) Modifier
                    .border(1.5.dp, color, CircleShape)
                    .padding(2.dp)
                else Modifier.padding(2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            tint = color,
            modifier = Modifier.size((size - 4).dp)
        )
    }
}

@Composable
private fun parseColor(colorStr: String?): Color {
    if (colorStr == null) return MaterialTheme.colorScheme.primary
    return try {
        Color(android.graphics.Color.parseColor(colorStr))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
}
