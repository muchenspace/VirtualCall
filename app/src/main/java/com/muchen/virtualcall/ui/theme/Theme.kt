package com.muchen.virtualcall.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OnAccentColor = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = OnAccentColor,
    primaryContainer = AccentPrimaryContainer,
    onPrimaryContainer = AccentPrimary,
    secondary = AccentSecondaryVal,
    onSecondary = OnAccentColor,
    secondaryContainer = AccentSecondaryContainer,
    onSecondaryContainer = AccentSecondaryVal,
    tertiary = StatusWarning,
    error = AccentRed,
    background = AppBgPrimary,
    onBackground = TextPrimary,
    surface = AppBgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = AppBgTertiary,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF475569),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFCBD5E1),
    secondary = Color(0xFF64748B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF94A3B8),
    tertiary = StatusWarning,
    error = AccentRed,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline = Color(0xFF2C2C2E),
    outlineVariant = Color(0xFF3A3A3C),
)

@Composable
fun VirtualCallTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }
    CompositionLocalProvider(LocalAppTheme provides LightTheme) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            content = content,
        )
    }
}

@Composable
fun CallScreenTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}

@Composable
fun OverlayTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppTheme provides LightTheme) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content,
        )
    }
}
