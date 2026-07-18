package com.financeapp.ui.theme

import androidx.compose.ui.unit.dp

/**
 * FinanceApp — 8-point grid spacing scale.
 *
 * Old money aesthetic: generous spacing creates "expensive" feel.
 * Never use raw .dp values in screens or components.
 */
object Spacing {
    /** 4.dp — icon gaps, tight padding inside chips/badges */
    val xs = 4.dp

    /** 8.dp — default gap between related items, inner padding */
    val sm = 8.dp

    /** 12.dp — medium-tight gap (between sm and md) */
    val smd = 12.dp

    /** 16.dp — standard content padding, card inner padding */
    val md = 16.dp

    /** 20.dp — slightly generous card padding */
    val mdLg = 20.dp

    /** 24.dp — section separator, screen title padding */
    val lg = 24.dp

    /** 32.dp — large section gap, hero element padding */
    val xl = 32.dp

    /** 48.dp — screen edge margin, major section divider */
    val xxl = 48.dp

    /** 64.dp — large icon size, hero illustration */
    val iconXl = 64.dp

    /** 48.dp — standard large icon */
    val iconLg = 48.dp

    /** 40.dp — medium icon container */
    val iconMd = 40.dp

    /** 32.dp — small icon container */
    val iconSm = 32.dp

    /** 24.dp — icon in list item */
    val iconXs = 24.dp

    /** 20.dp — small icon */
    val iconXxs = 20.dp

    /** 16.dp — tiny icon */
    val iconT = 16.dp
}
