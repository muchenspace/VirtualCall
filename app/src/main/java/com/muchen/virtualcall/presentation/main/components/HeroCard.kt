package com.muchen.virtualcall.presentation.main.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.ui.components.BadgeType
import com.muchen.virtualcall.ui.components.DangerButton
import com.muchen.virtualcall.ui.components.PrimaryButton
import com.muchen.virtualcall.ui.components.PulsingDot
import com.muchen.virtualcall.ui.components.StatusBadge
import com.muchen.virtualcall.ui.theme.LocalAppTheme

@Composable
internal fun HeroCard(
    armed: Boolean,
    accessibilityEnabled: Boolean,
    onToggleService: () -> Unit,
) {
    val colors = LocalAppTheme.current
    val accent = if (armed) colors.accent else colors.textSecondary
    val bgTertiary = colors.bgTertiary
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(28.dp.toPx()) } }
    val borderWidth = remember { with(density) { 1.dp.toPx() } }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(color = bgTertiary, cornerRadius = cornerRadius)
                drawRoundRect(
                    color = accent.copy(alpha = 0.2f),
                    cornerRadius = cornerRadius,
                    style = Stroke(width = borderWidth),
                )
            }
            .padding(24.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (armed) PulsingDot(color = colors.statusOnline)
                else Box(
                    Modifier.size(12.dp).clip(CircleShape)
                        .background(colors.textSecondary.copy(alpha = 0.6f))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(
                        if (armed) R.string.hero_service_running
                        else R.string.hero_service_idle
                    ),
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    text = stringResource(
                        if (accessibilityEnabled) R.string.badge_accessibility_on
                        else R.string.badge_accessibility_off
                    ),
                    type = if (accessibilityEnabled) BadgeType.SUCCESS else BadgeType.ERROR,
                )
                StatusBadge(
                    text = stringResource(
                        if (armed) R.string.badge_service_on
                        else R.string.badge_service_off
                    ),
                    type = if (armed) BadgeType.SUCCESS else BadgeType.WARNING,
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.RocketLaunch, null,
                    tint = accent, modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.hero_trigger_hint),
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
            Crossfade(
                targetState = armed,
                animationSpec = tween(200),
                label = "toggleBtn",
            ) { isArmed ->
                if (isArmed) {
                    DangerButton(
                        text = stringResource(R.string.btn_toggle_service_stop),
                        icon = Icons.Outlined.PowerSettingsNew,
                        onClick = onToggleService,
                    )
                } else {
                    PrimaryButton(
                        text = stringResource(R.string.btn_toggle_service_start),
                        icon = Icons.Outlined.PowerSettingsNew,
                        onClick = onToggleService,
                    )
                }
            }
        }
    }
}
