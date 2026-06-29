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

/**
 * 主界面浅色配色（MaterialTheme 层）。
 */
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

/**
 * 来电/通话界面深色配色（MaterialTheme 层）。仅用于通话页的 Material3 组件回退，
 * 实际通话页颜色由 [CallBgTop] / [CallAnswerGreen] 等硬编码常量控制。
 */
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

/**
 * 主界面主题：浅色 Slate 配色，状态栏图标深色。
 */
@Composable
fun VirtualCallTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = LightTheme.bgPrimary.toArgb()
            window.navigationBarColor = LightTheme.bgPrimary.toArgb()
            // 浅色背景：状态栏/导航栏图标深色
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

/**
 * 来电全屏界面专用主题：深色背景、透明系统栏、不强制深色模式。
 */
@Composable
fun CallScreenTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}

/**
 * 悬浮窗专用主题：运行在 Service 的 ComposeView 中，view.context 不是 Activity，
 * 因此不能像 [VirtualCallTheme]/[CallScreenTheme] 那样操作 Activity 的 window，
 * 否则会抛出 ClassCastException。这里仅提供 MaterialTheme 配色。
 */
@Composable
fun OverlayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
