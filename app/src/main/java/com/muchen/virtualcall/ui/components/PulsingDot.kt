package com.muchen.virtualcall.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

// 滑动状态：滑动中为 true，PulsingDot 等动画暂停以减少绘制开销
val LocalScrolling = compositionLocalOf { false }

/**
 * 呼吸动画状态点。纯 drawBehind 绘制层动画，零重组，滑动不卡顿。
 * 滚动时真正暂停动画协程，避免每帧驱动动画抢占主线程。
 */
@Composable
fun PulsingDot(
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppTheme.current
    val resolvedColor = if (color == Color.Unspecified) colors.statusOnline else color
    val isScrolling = LocalScrolling.current
    // 使用 Animatable 替代 rememberInfiniteTransition，滚动时可真正暂停协程
    val progress = remember { Animatable(0.5f) }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            // 滚动开始时固定到 0.5f，确保滚动结束后从此处平滑恢复
            progress.snapTo(0.5f)
        } else {
            // 滚动停止后才运行动画，避免每帧驱动动画协程抢占主线程
            while (true) {
                progress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
                progress.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
            }
        }
    }
    Box(
        modifier = modifier
            .size(12.dp)
            .drawBehind {
                // 滚动中读取固定值，不依赖动画状态，避免重绘
                val p = if (isScrolling) 0.5f else progress.value
                val maxR = size.minDimension / 2f
                drawCircle(
                    color = resolvedColor.copy(alpha = 0.15f + 0.25f * p),
                    radius = maxR * (0.8f + 0.4f * p),
                )
                drawCircle(
                    color = resolvedColor,
                    radius = maxR * 0.66f,
                )
            },
    )
}
