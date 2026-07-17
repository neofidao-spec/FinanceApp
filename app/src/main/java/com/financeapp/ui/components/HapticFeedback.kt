package com.financeapp.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Button that triggers haptic feedback on press.
 */
@Composable
fun HapticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    isLoading: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = if (isLoading) "Menyimpan..." else text,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

/**
 * Small icon button with haptic.
 */
@Composable
fun HapticIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    androidx.compose.material3.IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
    ) {
        content()
    }
}
