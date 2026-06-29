package com.muchen.virtualcall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

/**
 * 单选选项：极简 radio，选中态白色描边。drawBehind 绘制背景+边框减少 saveLayer。
 */
@Composable
fun RadioOption(
    label: String,
    description: String? = null,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = LocalAppTheme.current
    val borderColor = if (selected) colors.textPrimary else colors.borderSubtle
    val bgColor = if (selected) colors.textPrimary.copy(alpha = 0.06f) else Color.Transparent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.98f else 1f
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(16.dp.toPx()) } }
    val borderWidth = remember { with(density) { 1.dp.toPx() } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawRoundRect(color = bgColor, cornerRadius = cornerRadius)
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = borderWidth),
                )
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(22.dp).clip(CircleShape)
                .background(if (selected) colors.textPrimary else Color.Transparent)
                .border(2.dp, if (selected) colors.textPrimary else colors.textSecondary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(colors.textOnAccent))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                color = if (selected) colors.textPrimary else colors.textSecondary,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(description, color = colors.textSecondary.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}
