package com.muchen.virtualcall.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

/**
 * 主按钮：纯白底黑字，极简克制。drawBehind 绘制背景减少 saveLayer。
 */
@Composable
fun PrimaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalAppTheme.current
    val accent = colors.accent
    val textOnAccent = colors.textOnAccent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.97f else 1f
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(18.dp.toPx()) } }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawRoundRect(
                    color = if (enabled) accent else accent.copy(alpha = 0.3f),
                    cornerRadius = cornerRadius,
                )
            }
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = textOnAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = textOnAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * 次级按钮：灰描边 + 透明底，极简。drawBehind 绘制背景+边框减少 saveLayer。
 */
@Composable
fun SecondaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    val colors = LocalAppTheme.current
    val resolvedTint = if (tint == Color.Unspecified) colors.accentSecondary else tint
    val bgTertiary = colors.bgTertiary
    val borderDefault = colors.borderDefault
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.97f else 1f
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(16.dp.toPx()) } }
    val borderWidth = remember { with(density) { 1.dp.toPx() } }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                if (pressed) {
                    drawRoundRect(color = bgTertiary, cornerRadius = cornerRadius)
                }
                drawRoundRect(
                    color = borderDefault,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = borderWidth),
                )
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = resolvedTint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = resolvedTint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * 危险按钮：纯红，仅用于停止/挂断。drawBehind 绘制背景减少 saveLayer。
 */
@Composable
fun DangerButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppTheme.current
    val statusOffline = colors.statusOffline
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.97f else 1f
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(18.dp.toPx()) } }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawRoundRect(color = statusOffline, cornerRadius = cornerRadius)
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
