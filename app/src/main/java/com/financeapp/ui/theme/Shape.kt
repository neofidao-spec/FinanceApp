package com.financeapp.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * FinanceApp — Shape scale with variation.
 *
 * Hero Card: 24dp (premium feel)
 * Card: 18dp (standard)
 * Small Card: 16dp (compact)
 * Button: 14dp (rounded)
 * Chip/Badge: 8dp (small tags)
 * FAB: Circle
 */
val FinanceShapes = Shapes(
    /** 8.dp — chips, badges, small tags */
    extraSmall = RoundedCornerShape(8.dp),

    /** 14.dp — buttons, input fields */
    small = RoundedCornerShape(14.dp),

    /** 18.dp — standard cards, list items */
    medium = RoundedCornerShape(18.dp),

    /** 16.dp — compact cards, mini stat cards */
    large = RoundedCornerShape(16.dp),

    /** 24.dp — hero cards, summary cards, bottom sheets */
    extraLarge = RoundedCornerShape(24.dp)
)

/** FAB shape — perfect circle */
val FabShape = CircleShape
