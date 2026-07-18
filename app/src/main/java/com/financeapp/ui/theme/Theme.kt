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

// ── Semantic Finance Colors ────────────────────────────────────

/**
 * App-specific semantic colors for finance domain.
 * Access via MaterialTheme.financeColors.income etc.
 */
data class FinanceColors(
    /** Hijau — income, positive balance, savings */
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,

    /** Merah — expense, negative balance, overspend */
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,

    /** Kuning/amber — budget approaching limit */
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    /** Neutral — disabled, placeholder, divider */
    val neutral: Color,
    val onNeutral: Color
)

val LightFinanceColors = FinanceColors(
    income = Color(0xFF2E7D32),
    onIncome = Color.White,
    incomeContainer = Color(0xFFC8E6C9),
    onIncomeContainer = Color(0xFF1B5E20),

    expense = Color(0xFFC62828),
    onExpense = Color.White,
    expenseContainer = Color(0xFFFFCDD2),
    onExpenseContainer = Color(0xFFB71C1C),

    warning = Color(0xFFF57F17),
    onWarning = Color.White,
    warningContainer = Color(0xFFFFF9C4),
    onWarningContainer = Color(0xFFF57F17),

    neutral = Color(0xFF9E9E9E),
    onNeutral = Color.White
)

val DarkFinanceColors = FinanceColors(
    income = Color(0xFF81C784),
    onIncome = Color(0xFF1B5E20),
    incomeContainer = Color(0xFF2E7D32),
    onIncomeContainer = Color(0xFFC8E6C9),

    expense = Color(0xFFEF9A9A),
    onExpense = Color(0xFFB71C1C),
    expenseContainer = Color(0xFFC62828),
    onExpenseContainer = Color(0xFFFFCDD2),

    warning = Color(0xFFFFF176),
    onWarning = Color(0xFFF57F17),
    warningContainer = Color(0xFFF57F17),
    onWarningContainer = Color(0xFFFFF9C4),

    neutral = Color(0xFF757575),
    onNeutral = Color(0xFFE0E0E0)
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

/** Access finance colors: `MaterialTheme.financeColors.income` */
val androidx.compose.material3.ColorScheme.financeColors: FinanceColors
    @Composable
    get() = LocalFinanceColors.current

// ── Material3 Color Schemes ────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = Color(0xFF43A047),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF1B5E20),

    tertiary = Color(0xFFFF8F00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFE65100),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),

    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF616161),

    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),

    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF43A047),
    onSecondaryContainer = Color(0xFFC8E6C9),

    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFFE65100),
    tertiaryContainer = Color(0xFFFF8F00),
    onTertiaryContainer = Color(0xFFFFE0B2),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFCDD2),

    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242)
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
