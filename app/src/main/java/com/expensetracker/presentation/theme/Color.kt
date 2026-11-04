package com.expensetracker.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green500 = Color(0xFF2E7D32)
val Green600 = Color(0xFF1B5E20)
val Green400 = Color(0xFF4CAF50)
val Green100 = Color(0xFFC8E6C9)
val Green900 = Color(0xFF0D4715)

val Red500 = Color(0xFFC62828)
val Red600 = Color(0xFFB71C1C)
val Red400 = Color(0xFFEF5350)
val Red100 = Color(0xFFE57373)

val Blue500 = Color(0xFF1976D2)
val Blue600 = Color(0xFF1565C0)
val Blue400 = Color(0xFF42A5F5)
val Blue100 = Color(0xFFBBDEFB)

val Orange500 = Color(0xFFE65100)
val Orange600 = Color(0xFFBF360C)
val Orange400 = Color(0xFFFF9800)
val Orange100 = Color(0xFFFFE0B2)

val Purple500 = Color(0xFF7B1FA2)
val Purple600 = Color(0xFF6A1B9A)
val Purple400 = Color(0xFFBA68C8)
val Purple100 = Color(0xFFE1BEE7)

val Teal500 = Color(0xFF00796B)
val Teal600 = Color(0xFF00695C)
val Teal400 = Color(0xFF26A69A)
val Teal100 = Color(0xFFB2DFDB)

val Amber500 = Color(0xFFF57F17)
val Amber600 = Color(0xFFE65100)
val Amber400 = Color(0xFFFFC107)
val Amber100 = Color(0xFFFFF8E1)

private val LightColorScheme = lightColorScheme(
    primary = Green600, primaryContainer = Green100,
    secondary = Blue600, secondaryContainer = Blue100,
    tertiary = Orange600, tertiaryContainer = Orange100,
    error = Red600, errorContainer = Red100,
    surface = Color.White, surfaceDim = Color(0xFFF5F5F5),
    surfaceBright = Color.White, surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F5F5), surfaceContainer = Color(0xFFEEEEEE),
    surfaceContainerHigh = Color(0xFFE0E0E0), surfaceContainerHighest = Color(0xFFD6D6D6),
    onSurface = Color(0xFF1C1C1C), onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF757575), outlineVariant = Color(0xFFBDBDBD),
    inverseSurface = Color(0xFF313131), inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Green400, background = Color(0xFFFAFAFA), onBackground = Color(0xFF1C1C1C),
)

private val DarkColorScheme = darkColorScheme(
    primary = Green400, primaryContainer = Green900,
    secondary = Blue400, secondaryContainer = Blue600,
    tertiary = Orange400, tertiaryContainer = Orange600,
    error = Red400, errorContainer = Red900,
    surface = Color(0xFF1E1E1E), surfaceDim = Color(0xFF121212),
    surfaceBright = Color(0xFF2C2C2C), surfaceContainerLowest = Color(0xFF121212),
    surfaceContainerLow = Color(0xFF1E1E1E), surfaceContainer = Color(0xFF252525),
    surfaceContainerHigh = Color(0xFF333333), surfaceContainerHighest = Color(0xFF3E3E3E),
    onSurface = Color(0xFFE6E6E6), onSurfaceVariant = Color(0xFFC0C0C0),
    outline = Color(0xFF909090), outlineVariant = Color(0xFF5C5C5C),
    inverseSurface = Color(0xFFE6E6E6), inverseOnSurface = Color(0xFF313131),
    inversePrimary = Green600, background = Color(0xFF121212), onBackground = Color(0xFFE6E6E6),
)

@Composable
fun AppTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}