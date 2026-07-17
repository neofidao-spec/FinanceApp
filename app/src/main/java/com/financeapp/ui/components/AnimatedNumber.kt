package com.financeapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

/**
 * A composable that animates number changes with slide-up/slide-down effect.
 * Each digit animates independently for a realistic counter effect.
 *
 * @param value The numeric value to display (animated on change)
 * @param modifier Modifier for the composable
 * @param style TextStyle for the text
 * @param color Text color
 * @param format Lambda to format the number string. Defaults to Int.toString()
 */
@Composable
fun AnimatedNumber(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
    format: (Int) -> String = { it.toString() }
) {
    val formattedText = remember(value) { format(value) }

    Row(modifier = modifier) {
        formattedText.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> height } togetherWith
                            slideOutVertically { height -> -height }
                    } else {
                        slideInVertically { height -> -height } togetherWith
                            slideOutVertically { height -> height }
                    }.using(SizeTransform(clip = false))
                },
                label = "digit_animation_$index"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = style,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Overload for Double values (e.g., currency display)
 */
@Composable
fun AnimatedNumber(
    value: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
    format: (Double) -> String = { it.toLong().toString() }
) {
    val formattedText = remember(value) { format(value) }

    Row(modifier = modifier) {
        formattedText.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> height } togetherWith
                            slideOutVertically { height -> -height }
                    } else {
                        slideInVertically { height -> -height } togetherWith
                            slideOutVertically { height -> height }
                    }.using(SizeTransform(clip = false))
                },
                label = "digit_animation_$index"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = style,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedNumberPreview() {
    MaterialTheme {
        AnimatedNumber(
            value = 1250000,
            style = TextStyle(fontSize = 32.sp),
            color = MaterialTheme.colorScheme.primary,
            format = { "Rp ${String.format("%,.0f", it.toDouble())}" }
        )
    }
}
