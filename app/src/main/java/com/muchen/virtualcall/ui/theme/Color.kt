package com.muchen.virtualcall.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============ Slate 冷灰配色系统 ============
// 设计理念：白灰为主调，融入微妙蓝色冷调构建现代极简感
// 仅保留语义色（绿/红/橙）用于状态指示，不引入装饰性彩色

// 背景层次（从浅到略深）
val AppBgPrimary = Color(0xFFF8FAFC)      // slate-50, 主背景
val AppBgSecondary = Color(0xFFFFFFFF)    // 纯白, 卡片背景
val AppBgTertiary = Color(0xFFF1F5F9)    // slate-100, 内嵌/悬浮

// 强调色：冷灰蓝（主操作）
val AccentPrimary = Color(0xFF475569)     // slate-600, 主强调
val AccentPrimaryDark = Color(0xFF334155) // slate-700, 按压态
val AccentPrimaryContainer = Color(0xFFE2E8F0) // slate-200, 浅强调底

// 辅助色：中灰（次级操作）
val AccentSecondaryVal = Color(0xFF64748B) // slate-500
val AccentSecondaryContainer = Color(0xFFF1F5F9) // slate-100

// 危险色：语义红（仅用于挂断/危险）
val AccentRed = Color(0xFFEF4444)         // red-500

// 文字层次
val TextPrimary = Color(0xFF0F172A)       // slate-900, 近黑
val TextSecondary = Color(0xFF64748B)     // slate-500, 中灰
val TextOnAccent = Color(0xFFFFFFFF)      // 白字在深色按钮上

// 边框/分隔
val BorderSubtle = Color(0xFFF1F5F9)      // slate-100
val BorderDefault = Color(0xFFE2E8F0)     // slate-200
val BorderStrong = Color(0xFFCBD5E1)      // slate-300

// 状态色（语义指示，低频使用）
val StatusOnline = Color(0xFF059669)      // emerald-600, 服务运行
val StatusOffline = Color(0xFFEF4444)     // red-500
val StatusWarning = Color(0xFFD97706)     // amber-600
val StatusInfo = Color(0xFF64748B)        // slate-500

// ============ 来电界面配色（深色仿真，不随主题切换） ============
val CallBgTop = Color(0xFF1C1C1E)
val CallBgBottom = Color(0xFF000000)
val CallAnswerGreen = Color(0xFF34C759)   // 接听键用绿
val CallDeclineRed = Color(0xFFFF453A)    // 挂断键用红
val CallTextSubtitle = Color(0xFFAEAEB2)
val CallSecondary = Color(0x33FFFFFF)
val CallPhoneNumber = Color(0xFFAEAEB2)
val CallChipBg = Color(0x1AFFFFFF)

// ============ 主题色集合 ============
/**
 * 应用主题色集合。当前为单一浅色 Slate 主题，
 * UI 中用 `AppTheme.current.bgPrimary` 等访问。
 */
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
