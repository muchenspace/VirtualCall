package com.muchen.virtualcall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

/**
 * 设置行：图标 + 标题 + 副标题 + 右箭头。drawBehind 绘制按压背景减少 saveLayer。
 */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    iconTint: Color = Color.Unspecified,
    subtitleColor: Color = Color.Unspecified,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = LocalAppTheme.current
    val resolvedIconTint = if (iconTint == Color.Unspecified) colors.accent else iconTint
    val resolvedSubtitleColor = if (subtitleColor == Color.Unspecified) colors.textSecondary else subtitleColor
    val bgTertiary = colors.bgTertiary
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(16.dp.toPx()) } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (pressed) 0.85f else 1f }
            .drawBehind {
                if (pressed) {
                    drawRoundRect(color = bgTertiary, cornerRadius = cornerRadius)
                }
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(resolvedIconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = resolvedIconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = resolvedSubtitleColor, fontSize = 12.sp)
            }
        }
        if (trailing != null) trailing()
        else Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp),
        )
    }
}
