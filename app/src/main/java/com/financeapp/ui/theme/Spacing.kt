package com.financeapp.ui.theme

import androidx.compose.ui.unit.dp

/**
 * FinanceApp Design System — 8-point grid spacing scale.
 *
 * Every padding, margin, gap, and offset in the app MUST reference
 * one of these tokens. Never use raw .dp values in screens or components.
 */
object Spacing {
    /** 4.dp — icon gaps, tight padding inside chips/badges */
    val xs = 4.dp

    /** 8.dp — default gap between related items, inner padding */
    val sm = 8.dp

    /** 16.dp — standard content padding, card inner padding, section gap */
    val md = 16.dp

    /** 24.dp — section separator, top-level screen padding */
    val lg = 24.dp

    /** 32.dp — large section gap, hero element padding */
    val xl = 32.dp

    /** 48.dp — screen edge margin, major section divider */
    val xxl = 48.dp
}
