package com.financeapp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * FinanceApp Design System — Motion tokens.
 *
 * All animation durations MUST come from here.
 * Never use raw tween(N) in screens or components.
 */
object MotionTokens {
    /** 150ms — micro-interaction: button press, toggle, chip select */
    const val MICRO = 150

    /** 200ms — standard transition: color change, opacity, swipe action */
    const val SHORT = 200

    /** 300ms — content transition: page enter, card appear, state change */
    const val MEDIUM = 300

    /** 500ms — emphasis: progress bar, ring fill, number count-up */
    const val LONG = 500

    /** 800ms — dramatic: large chart reveal, full-screen transition */
    const val EXTRA_LONG = 800

    /** 1200ms — infinite loop: shimmer, loading pulse */
    const val INFINITE = 1200

    // ── Easing ──────────────────────────────────────────────

    /** Standard ease — most transitions */
    val standardEase = FastOutSlowInEasing

    /** Emphasized ease — enter transitions (decelerate in) */
    val emphasizedEase = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
