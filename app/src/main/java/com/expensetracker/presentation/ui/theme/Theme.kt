package com.expensetracker.presentation.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.expensetracker.domain.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ExpenseTrackerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isSystemInDarkTheme = isSystemInDarkTheme()

    SideEffect {
        val window = view.getWindow()
        val isDark = when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        CoroutineScope(Dispatchers.Main).launch {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            val controller = window.insetsController
            controller?.isAppearanceLightStatusBars = !isDark
            controller?.isAppearanceLightNavigationBars = !isDark
        }
    }

    val lightColorScheme = remember(context) { LightColorScheme }
    val darkColorScheme = remember(context) { DarkColorScheme }

    val dynamicLightColorScheme = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else null
    }
    val dynamicDarkColorScheme = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else null
    }

    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> dynamicLightColorScheme ?: lightColorScheme
        ThemeMode.DARK -> dynamicDarkColorScheme ?: darkColorScheme
        ThemeMode.SYSTEM -> {
            if (isSystemInDarkTheme) {
                dynamicDarkColorScheme ?: darkColorScheme
            } else {
                dynamicLightColorScheme ?: lightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: androidx.compose.ui.graphics.drawscope.Stroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        content = content
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: androidx.compose.ui.graphics.drawscope.Stroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = shape),
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        content = content
    )
}

@Composable
fun AppElevatedCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: androidx.compose.ui.graphics.drawscope.Stroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = shape),
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        content = content
    )
}
