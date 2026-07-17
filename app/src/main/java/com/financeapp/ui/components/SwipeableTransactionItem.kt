package com.financeapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: TransactionWithCategory,
    onSwipeEdit: () -> Unit,
    onSwipeDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSwipeDelete()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.StartToEnd -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSwipeEdit()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.Settled -> { /* no-op */ }
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val deleteColor by animateColorAsState(
                targetValue = when {
                    direction == SwipeToDismissBoxValue.EndToStart &&
                    dismissState.targetValue == SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                    else -> Color(0xFFF44336).copy(alpha = 0.3f)
                },
                animationSpec = tween(200), label = "deleteColor"
            )
            val editColor by animateColorAsState(
                targetValue = when {
                    direction == SwipeToDismissBoxValue.StartToEnd &&
                    dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1565C0)
                    else -> Color(0xFF1565C0).copy(alpha = 0.3f)
                },
                animationSpec = tween(200), label = "editColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> deleteColor
                            SwipeToDismissBoxValue.StartToEnd -> editColor
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.Center
                }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Hapus", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.Delete, "Hapus transaksi", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Edit, "Edit transaksi", tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    else -> { /* empty */ }
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        // Content: same visual as the original TransactionCard
        val isIncome = transaction.transaction.type == TransactionType.INCOME
        val icon = FinanceIcons.getIcon(transaction.category.name)
        val iconColor = FinanceIcons.getColorFromHex(transaction.category.color)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.category.name,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (transaction.transaction.description.isNotBlank()) {
                        Text(
                            text = transaction.transaction.description,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val prefix = if (isIncome) "+" else "-"
                    Text(
                        text = "$prefix ${FormatterUtil.formatCurrency(transaction.transaction.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5722)
                    )
                    Text(
                        text = transaction.transaction.date.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
