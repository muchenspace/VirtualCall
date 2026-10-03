package com.muchen.virtualcall.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val AppBgPrimary = Color(0xFFF8FAFC)
val AppBgSecondary = Color(0xFFFFFFFF)
val AppBgTertiary = Color(0xFFF1F5F9)

val AccentPrimary = Color(0xFF475569)
val AccentPrimaryDark = Color(0xFF334155)
val AccentPrimaryContainer = Color(0xFFE2E8F0)

val AccentSecondaryVal = Color(0xFF64748B)
val AccentSecondaryContainer = Color(0xFFF1F5F9)

val AccentRed = Color(0xFFEF4444)

val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val TextOnAccent = Color(0xFFFFFFFF)

val BorderSubtle = Color(0xFFF1F5F9)
val BorderDefault = Color(0xFFE2E8F0)
val BorderStrong = Color(0xFFCBD5E1)

val StatusOnline = Color(0xFF059669)
val StatusOffline = Color(0xFFEF4444)
val StatusWarning = Color(0xFFD97706)
val StatusInfo = Color(0xFF64748B)

val CallBgTop = Color(0xFF1C1C1E)
val CallBgBottom = Color(0xFF000000)
val CallAnswerGreen = Color(0xFF34C759)
val CallDeclineRed = Color(0xFFFF453A)
val CallTextSubtitle = Color(0xFFAEAEB2)
val CallSecondary = Color(0x33FFFFFF)
val CallPhoneNumber = Color(0xFFAEAEB2)
val CallChipBg = Color(0x1AFFFFFF)

@Immutable
data class AppThemeColors(
    val bgPrimary: Color,
    val bgSecondary: Color,
    val bgTertiary: Color,
    val accent: Color,
    val accentDark: Color,
    val accentContainer: Color,
    val accentSecondary: Color,
    val accentSecondaryContainer: Color,
    val borderDefault: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val statusOnline: Color,
    val statusOffline: Color,
    val statusWarning: Color,
    val statusInfo: Color,
    val textOnAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)

val LightTheme = AppThemeColors(
    bgPrimary = AppBgPrimary,
    bgSecondary = AppBgSecondary,
    bgTertiary = AppBgTertiary,
    accent = AccentPrimary,
    accentDark = AccentPrimaryDark,
    accentContainer = AccentPrimaryContainer,
    accentSecondary = AccentSecondaryVal,
    accentSecondaryContainer = AccentSecondaryContainer,
    borderDefault = BorderDefault,
    borderSubtle = BorderSubtle,
    borderStrong = BorderStrong,
    statusOnline = StatusOnline,
    statusOffline = StatusOffline,
    statusWarning = StatusWarning,
    statusInfo = StatusInfo,
    textOnAccent = TextOnAccent,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
)

val LocalAppTheme = staticCompositionLocalOf { LightTheme }

val AppTheme: AppThemeColors
    @Composable get() = LocalAppTheme.current
