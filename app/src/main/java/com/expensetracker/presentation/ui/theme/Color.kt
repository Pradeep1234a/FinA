package com.expensetracker.presentation.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Material 3 Light Color Scheme
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainerHighest = Color(0xFFE6E0E9),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainer = Color(0xFFF1E9EF),
    surfaceContainerLow = Color(0xFFF7EDF8),
    surfaceContainerLowest = Color.White,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFD0BCFF),
    surfaceTint = Color(0xFF6750A4),
    primaryFixed = Color(0xFFEADDFF),
    onPrimaryFixed = Color(0xFF21005D),
    primaryFixedDim = Color(0xFFD0BCFF),
    onPrimaryFixedVariant = Color(0xFF4F378B),
    secondaryFixed = Color(0xFFE8DEF8),
    onSecondaryFixed = Color(0xFF1D192B),
    secondaryFixedDim = Color(0xFFCCC2DC),
    onSecondaryFixedVariant = Color(0xFF4A4458),
    tertiaryFixed = Color(0xFFFFD8E4),
    onTertiaryFixed = Color(0xFF31111D),
    tertiaryFixedDim = Color(0xFFEFB8C8),
    onTertiaryFixedVariant = Color(0xFF633B48)
)

// Material 3 Dark Color Scheme
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainerHighest = Color(0xFF38363A),
    surfaceContainerHigh = Color(0xFF2E2D30),
    surfaceContainer = Color(0xFF252428),
    surfaceContainerLow = Color(0xFF211F24),
    surfaceContainerLowest = Color(0xFF161519),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF6750A4),
    surfaceTint = Color(0xFFD0BCFF),
    primaryFixed = Color(0xFFEADDFF),
    onPrimaryFixed = Color(0xFF21005D),
    primaryFixedDim = Color(0xFFD0BCFF),
    onPrimaryFixedVariant = Color(0xFF4F378B),
    secondaryFixed = Color(0xFFE8DEF8),
    onSecondaryFixed = Color(0xFF1D192B),
    secondaryFixedDim = Color(0xFFCCC2DC),
    onSecondaryFixedVariant = Color(0xFF4A4458),
    tertiaryFixed = Color(0xFFFFD8E4),
    onTertiaryFixed = Color(0xFF31111D),
    tertiaryFixedDim = Color(0xFFEFB8C8),
    onTertiaryFixedVariant = Color(0xFF633B48)
)

// Category Colors
val CategoryColors = listOf(
    Color(0xFFFF6B6B), // Food & Dining
    Color(0xFF4ECDC4), // Transportation
    Color(0xFF45B7D1), // Shopping
    Color(0xFF96CEB4), // Entertainment
    Color(0xFFFFEAA7), // Bills & Utilities
    Color(0xFFDDA0DD), // Healthcare
    Color(0xFF98D8C8), // Education
    Color(0xFFF7DC6F), // Travel
    Color(0xFF2ECC71), // Salary
    Color(0xFF27AE60), // Freelance
    Color(0xFF1ABC9C), // Investments
    Color(0xFF16A085)  // Gifts
)

// Semantic Colors
val IncomeColor = Color(0xFF2ECC71)
val ExpenseColor = Color(0xFFE74C3C)
val BudgetSafeColor = Color(0xFF2ECC71)
val BudgetWarningColor = Color(0xFFF39C12)
val BudgetDangerColor = Color(0xFFE74C3C)

@Composable
fun AppColors(isDark: Boolean): MaterialTheme.colors.() -> Unit {
    val colors = if (isDark) DarkColorScheme else LightColorScheme
    return @Composable { this.colors = colors }
}

@Composable
fun dynamicColors(context: Context): MaterialTheme.colors.() -> Unit {
    // Dynamic colors are handled by Material3 automatically on Android 12+
    return @Composable { }
}
