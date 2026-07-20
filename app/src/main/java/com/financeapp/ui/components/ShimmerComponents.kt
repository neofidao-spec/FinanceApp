package com.financeapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing

/**
 * Modifier that applies a shimmer effect — a moving gradient highlight.
 * Note: Uses RoundedCornerShape(Spacing.xs) since composed{} is not a @Composable context.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            Color.Transparent
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 200f, 0f)
    )

    this.then(
        Modifier.background(
            shimmerBrush,
            RoundedCornerShape(Spacing.xs)
        )
    )
}

/**
 * Skeleton card — shimmer placeholder for a balance/amount card.
 */
@Composable
fun ShimmerBalanceCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(Spacing.md)
                .clip(RoundedCornerShape(Spacing.xs))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(Spacing.lg)
                .clip(RoundedCornerShape(Spacing.xs))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(Spacing.smd)
                .clip(RoundedCornerShape(Spacing.xs))
                .shimmerEffect()
        )
    }
}

/**
 * Skeleton transaction item — shimmer placeholder for a list item row.
 */
@Composable
fun ShimmerTransactionItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.smd),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd)
    ) {
        // Icon placeholder
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .shimmerEffect()
        )
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(Spacing.smd)
                    .clip(RoundedCornerShape(Spacing.xs))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(Spacing.smd)
                    .clip(RoundedCornerShape(Spacing.xs))
                    .shimmerEffect()
            )
        }
        // Amount placeholder
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(Spacing.md)
                .clip(RoundedCornerShape(Spacing.xs))
                .shimmerEffect()
        )
    }
}
