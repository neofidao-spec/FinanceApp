package com.financeapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Old Money Palette ─────────────────────────────────────────
// Elegant, warm, sophisticated. Tidak ada warna neon/terang.
// Cream backgrounds, charcoal text, copper/gold accents.

object FinancePalette {
    // Primary — Deep navy/slate (trust, authority)
    val Navy = Color(0xFF1B2A4A)
    val Slate = Color(0xFF2D4059)
    val SteelBlue = Color(0xFF4A6580)

    // Surface — Warm whites (NOT cold #FFFFFF)
    val Cream = Color(0xFFFAF8F5)
    val Ivory = Color(0xFFF5F0EB)
    val Parchment = Color(0xFFEDE8E0)
    val WarmWhite = Color(0xFFFDFCFA)

    // Text — Charcoal family (NOT pure black)
    val Charcoal = Color(0xFF2C2A28)
    val WarmGray = Color(0xFF6B5A4D)
    val LightGray = Color(0xFF9E9488)
    val Divider = Color(0xFFD4CFC8)

    // Accent — Copper/Gold (premium, sparingly)
    val Copper = Color(0xFFB87333)
    val Champagne = Color(0xFFC9A86A)
    val GoldLight = Color(0xFFE8D5B0)

    // Semantic — Income/Expense (muted, sophisticated)
    val ForestGreen = Color(0xFF2E6B4F)
    val ForestGreenLight = Color(0xFF4A8B6A)
    val Burgundy = Color(0xFF7A2E3B)
    val BurgundyLight = Color(0xFFA04858)

    // Warning — Amber muted
    val Amber = Color(0xFFC49A3C)
    val AmberLight = Color(0xFFE8C878)

    // Error — Deep red
    val DeepRed = Color(0xFF8B2E3B)
    val DeepRedLight = Color(0xFFB84858)

    // Dark mode variants
    val DarkNavy = Color(0xFF0D1520)
    val DarkSurface = Color(0xFF1A1E28)
    val DarkSurfaceVariant = Color(0xFF252A36)
    val DarkText = Color(0xFFE8E4DE)
    val DarkTextSecondary = Color(0xFF9E9488)
    val DarkCopper = Color(0xFFD4956A)
    val DarkChampagne = Color(0xFFE8C878)
}

// ── Semantic Finance Colors ────────────────────────────────────

data class FinanceColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val neutral: Color,
    val onNeutral: Color
)

val LightFinanceColors = FinanceColors(
    income = FinancePalette.ForestGreen,
    onIncome = FinancePalette.WarmWhite,
    incomeContainer = Color(0xFFD7E7DD),
    onIncomeContainer = Color(0xFF1A3D2E),

    expense = FinancePalette.Burgundy,
    onExpense = FinancePalette.WarmWhite,
    expenseContainer = Color(0xFFF0D5D8),
    onExpenseContainer = Color(0xFF5A1E2B),

    warning = FinancePalette.Amber,
    onWarning = FinancePalette.WarmWhite,
    warningContainer = FinancePalette.AmberLight,
    onWarningContainer = Color(0xFF6B4A10),

    accent = FinancePalette.Copper,
    onAccent = FinancePalette.WarmWhite,
    neutral = FinancePalette.WarmGray,
    onNeutral = FinancePalette.WarmWhite
)

val DarkFinanceColors = FinanceColors(
    income = Color(0xFF6ABF8A),
    onIncome = Color(0xFF1A3D2E),
    incomeContainer = Color(0xFF2E6B4F),
    onIncomeContainer = Color(0xFFD7E7DD),

    expense = Color(0xFFD4788A),
    onExpense = Color(0xFF5A1E2B),
    expenseContainer = Color(0xFF7A2E3B),
    onExpenseContainer = Color(0xFFF0D5D8),

    warning = FinancePalette.AmberLight,
    onWarning = Color(0xFF6B4A10),
    warningContainer = FinancePalette.Amber,
    onWarningContainer = FinancePalette.AmberLight,

    accent = FinancePalette.DarkCopper,
    onAccent = Color(0xFF2C2A28),
    neutral = FinancePalette.DarkTextSecondary,
    onNeutral = FinancePalette.DarkText
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

val androidx.compose.material3.ColorScheme.financeColors: FinanceColors
    @Composable
    get() = LocalFinanceColors.current

// ── Material3 Color Schemes — Old Money ────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = FinancePalette.Navy,
    onPrimary = FinancePalette.WarmWhite,
    primaryContainer = FinancePalette.SteelBlue,
    onPrimaryContainer = FinancePalette.WarmWhite,

    secondary = FinancePalette.Copper,
    onSecondary = FinancePalette.WarmWhite,
    secondaryContainer = FinancePalette.GoldLight,
    onSecondaryContainer = FinancePalette.Charcoal,

    tertiary = FinancePalette.Champagne,
    onTertiary = FinancePalette.Charcoal,
    tertiaryContainer = Color(0xFFF0E8D8),
    onTertiaryContainer = FinancePalette.Charcoal,

    background = FinancePalette.Cream,
    onBackground = FinancePalette.Charcoal,

    surface = FinancePalette.WarmWhite,
    onSurface = FinancePalette.Charcoal,
    surfaceVariant = FinancePalette.Ivory,
    onSurfaceVariant = FinancePalette.WarmGray,

    error = FinancePalette.DeepRed,
    onError = FinancePalette.WarmWhite,
    errorContainer = Color(0xFFF0D5D8),
    onErrorContainer = FinancePalette.DeepRed,

    outline = FinancePalette.Divider,
    outlineVariant = FinancePalette.Parchment,

    inverseSurface = FinancePalette.Charcoal,
    inverseOnSurface = FinancePalette.Cream,
    inversePrimary = FinancePalette.SteelBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AAFCF),
    onPrimary = FinancePalette.DarkNavy,
    primaryContainer = FinancePalette.Slate,
    onPrimaryContainer = Color(0xFFD4E0F0),

    secondary = FinancePalette.DarkCopper,
    onSecondary = FinancePalette.DarkNavy,
    secondaryContainer = Color(0xFF5A4030),
    onSecondaryContainer = FinancePalette.GoldLight,

    tertiary = FinancePalette.DarkChampagne,
    onTertiary = FinancePalette.DarkNavy,
    tertiaryContainer = Color(0xFF4A3A20),
    onTertiaryContainer = FinancePalette.GoldLight,

    background = FinancePalette.DarkNavy,
    onBackground = FinancePalette.DarkText,

    surface = FinancePalette.DarkSurface,
    onSurface = FinancePalette.DarkText,
    surfaceVariant = FinancePalette.DarkSurfaceVariant,
    onSurfaceVariant = FinancePalette.DarkTextSecondary,

    error = FinancePalette.DeepRedLight,
    onError = FinancePalette.WarmWhite,
    errorContainer = FinancePalette.DeepRed,
    onErrorContainer = Color(0xFFF0D5D8),

    outline = Color(0xFF3A3E4A),
    outlineVariant = FinancePalette.DarkSurfaceVariant,

    inverseSurface = FinancePalette.DarkText,
    inverseOnSurface = FinancePalette.DarkNavy,
    inversePrimary = FinancePalette.Navy
)

// ── Theme Entry Point ──────────────────────────────────────────

@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val financeColors = if (darkTheme) DarkFinanceColors else LightFinanceColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalFinanceColors provides financeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = FinanceShapes,
            content = content
        )
    }
}
