package com.muchen.virtualcall.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

/**
 * 现代极简卡片：大圆角 + 极淡边框，drawBehind 单层绘制减少 overdraw。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppTheme.current
    val density = LocalDensity.current
    val cornerRadius = remember { with(density) { CornerRadius(28.dp.toPx()) } }
    val borderWidth = remember { with(density) { 1.dp.toPx() } }
    val bgSecondary = colors.bgSecondary
    val borderSubtle = colors.borderSubtle
    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(color = bgSecondary, cornerRadius = cornerRadius)
                drawRoundRect(
                    color = borderSubtle,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = borderWidth),
                )
            }
            .padding(20.dp),
    ) {
        Column(content = content)
    }
}
