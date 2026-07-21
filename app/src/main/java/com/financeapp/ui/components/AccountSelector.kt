package com.financeapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                        BorderStroke(
                            2.dp,
                            parseColor(account.color)
                        )
                    } else {
                        BorderStroke(
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