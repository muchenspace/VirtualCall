package com.muchen.virtualcall.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.ui.components.SectionCard
import com.muchen.virtualcall.ui.components.SettingRow
import com.muchen.virtualcall.ui.theme.LocalAppTheme

@Composable
internal fun PowerCard(
    accessibilityEnabled: Boolean,
    batteryOptimizationIgnored: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenBatteryOptimization: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onOpenLockScreenGuide: () -> Unit,
) {
    val colors = LocalAppTheme.current
    SectionCard(
        title = stringResource(R.string.label_power),
        subtitle = stringResource(R.string.subtitle_power),
        icon = Icons.Outlined.BatteryFull,
    ) {
        // 可自动检测的权限在前
        SettingRow(
            icon = Icons.Outlined.Settings,
            title = stringResource(R.string.action_accessibility_settings),
            subtitle = stringResource(R.string.subtitle_accessibility),
            onClick = onOpenAccessibilitySettings,
            iconTint = colors.accent,
            trailing = { StatusDot(granted = accessibilityEnabled) },
        )
        SettingRow(
            icon = Icons.Outlined.BatteryFull,
            title = stringResource(R.string.btn_battery_optimization),
            subtitle = stringResource(R.string.subtitle_battery),
            onClick = onOpenBatteryOptimization,
            iconTint = colors.accent,
            trailing = { StatusDot(granted = batteryOptimizationIgnored) },
        )
        // 不可自动检测的 ROM 特定权限在后
        SettingRow(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.action_lock_screen),
            subtitle = stringResource(R.string.subtitle_lock_screen),
            onClick = onOpenLockScreenGuide,
            iconTint = colors.accent,
        )
        SettingRow(
            icon = Icons.Outlined.Security,
            title = stringResource(R.string.btn_auto_start_settings),
            subtitle = stringResource(R.string.subtitle_autostart),
            onClick = onOpenAutoStartSettings,
            iconTint = colors.accent,
            subtitleColor = Color(0xFFFF3B30),
        )
    }
}

/** 权限状态圆点：绿色+对勾表示已开启，红色表示未开启。 */
@Composable
private fun StatusDot(granted: Boolean) {
    val color = if (granted) Color(0xFF34C759) else Color(0xFFFF3B30)
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (granted) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
