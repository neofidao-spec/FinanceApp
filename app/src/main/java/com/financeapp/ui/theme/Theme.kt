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

// ── Modern Finance Palette ─────────────────────────────────────
// Clean, light, professional. Tidak terlalu gelap.
// White cards, colored accents, generous whitespace.

object FinancePalette {
    // Primary — Muted blue (trust, professional)
    val Primary = Color(0xFF355C7D)
    val PrimaryLight = Color(0xFF5E81AC)
    val PrimaryDark = Color(0xFF2A4A65)

    // Surface — Clean whites
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)
    val SurfaceDim = Color(0xFFE2E8F0)

    // Text — Soft charcoal (NOT pure black)
    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val TextTertiary = Color(0xFF94A3B8)
    val TextDisabled = Color(0xFFCBD5E1)

    // Semantic — Vibrant but controlled
    val Success = Color(0xFF22C55E)
    val SuccessLight = Color(0xFFDCFCE7)
    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFEF3C7)
    val Danger = Color(0xFFEF4444)
    val DangerLight = Color(0xFFFEE2E2)
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFFDBEAFE)

    // Accent — Copper/Gold (premium touches)
    val Accent = Color(0xFFB87333)
    val AccentLight = Color(0xFFF5E6D3)

    // Divider
    val Divider = Color(0xFFE2E8F0)

    // Dark mode
    val DarkBackground = Color(0xFF0F172A)
    val DarkSurface = Color(0xFF1E293B)
    val DarkSurfaceVariant = Color(0xFF334155)
    val DarkTextPrimary = Color(0xFFF1F5F9)
    val DarkTextSecondary = Color(0xFF94A3B8)
    val DarkPrimary = Color(0xFF7DA8CC)
    val DarkPrimaryLight = Color(0xFF93C5FD)
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
    income = FinancePalette.Success,
    onIncome = Color.White,
    incomeContainer = FinancePalette.SuccessLight,
    onIncomeContainer = Color(0xFF166534),

    expense = FinancePalette.Danger,
    onExpense = Color.White,
    expenseContainer = FinancePalette.DangerLight,
    onExpenseContainer = Color(0xFF991B1B),

    warning = FinancePalette.Warning,
    onWarning = Color.White,
    warningContainer = FinancePalette.WarningLight,
    onWarningContainer = Color(0xFF92400E),

    accent = FinancePalette.Accent,
    onAccent = Color.White,
    neutral = FinancePalette.TextSecondary,
    onNeutral = Color.White
)

val DarkFinanceColors = FinanceColors(
    income = Color(0xFF4ADE80),
    onIncome = Color(0xFF166534),
    incomeContainer = Color(0xFF166534),
    onIncomeContainer = Color(0xFFDCFCE7),

    expense = Color(0xFFFCA5A5),
    onExpense = Color(0xFF991B1B),
    expenseContainer = Color(0xFF991B1B),
    onExpenseContainer = Color(0xFFFEE2E2),

    warning = Color(0xFFFCD34D),
    onWarning = Color(0xFF92400E),
    warningContainer = Color(0xFF92400E),
    onWarningContainer = Color(0xFFFEF3C7),

    accent = Color(0xFFD4956A),
    onAccent = Color(0xFF2C2A28),
    neutral = FinancePalette.DarkTextSecondary,
    onNeutral = FinancePalette.DarkTextPrimary
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

val androidx.compose.material3.ColorScheme.financeColors: FinanceColors
    @Composable
    get() = LocalFinanceColors.current

// ── Material3 Color Schemes — Modern Finance ───────────────────

private val LightColorScheme = lightColorScheme(
    primary = FinancePalette.Primary,
    onPrimary = Color.White,
    primaryContainer = FinancePalette.InfoLight,
    onPrimaryContainer = FinancePalette.PrimaryDark,

    secondary = FinancePalette.PrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF3730A3),

    tertiary = FinancePalette.Accent,
    onTertiary = Color.White,
    tertiaryContainer = FinancePalette.AccentLight,
    onTertiaryContainer = Color(0xFF7C2D12),

    background = FinancePalette.Background,
    onBackground = FinancePalette.TextPrimary,

    surface = FinancePalette.Surface,
    onSurface = FinancePalette.TextPrimary,
    surfaceVariant = FinancePalette.SurfaceVariant,
    onSurfaceVariant = FinancePalette.TextSecondary,

    error = FinancePalette.Danger,
    onError = Color.White,
    errorContainer = FinancePalette.DangerLight,
    onErrorContainer = Color(0xFF991B1B),

    outline = FinancePalette.Divider,
    outlineVariant = FinancePalette.SurfaceDim,

    inverseSurface = FinancePalette.TextPrimary,
    inverseOnSurface = FinancePalette.Background,
    inversePrimary = FinancePalette.PrimaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = FinancePalette.DarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = FinancePalette.Primary,
    onPrimaryContainer = Color(0xFFDBEAFE),

    secondary = FinancePalette.DarkPrimaryLight,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = FinancePalette.PrimaryLight,
    onSecondaryContainer = Color(0xFFE0E7FF),

    tertiary = FinancePalette.Accent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF7C2D12),
    onTertiaryContainer = FinancePalette.AccentLight,

    background = FinancePalette.DarkBackground,
    onBackground = FinancePalette.DarkTextPrimary,

    surface = FinancePalette.DarkSurface,
    onSurface = FinancePalette.DarkTextPrimary,
    surfaceVariant = FinancePalette.DarkSurfaceVariant,
    onSurfaceVariant = FinancePalette.DarkTextSecondary,

    error = Color(0xFFFCA5A5),
    onError = Color(0xFF991B1B),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),

    outline = Color(0xFF475569),
    outlineVariant = FinancePalette.DarkSurfaceVariant,

    inverseSurface = FinancePalette.DarkTextPrimary,
    inverseOnSurface = FinancePalette.DarkBackground,
    inversePrimary = FinancePalette.Primary
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
            val window = (view.context as? Activity)?.window ?: return@SideEffect
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
