package com.financeapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * FinanceApp Design System — Shape scale.
 *
 * Four tiers only. Every shape in the app MUST come from
 * MaterialTheme.shapes — never use raw RoundedCornerShape() in screens.
 */
val FinanceShapes = Shapes(
    /** 8.dp — chips, badges, small tags */
    extraSmall = RoundedCornerShape(8.dp),

    /** 12.dp — text fields, list items, small cards */
    small = RoundedCornerShape(12.dp),

    /** 16.dp — primary cards, dialogs */
    medium = RoundedCornerShape(16.dp),

    /** 24.dp — bottom sheets, large dialogs, hero cards */
    large = RoundedCornerShape(24.dp)
)
