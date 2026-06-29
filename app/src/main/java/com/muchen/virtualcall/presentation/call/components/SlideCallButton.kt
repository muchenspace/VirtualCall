package com.muchen.virtualcall.presentation.call.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/** 三重向上箭头指示器（挂断用红色，接听用绿色），滑动时渐隐 */
@Composable
private fun TripleChevron(color: Color, alpha: Float = 1f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        repeat(3) { index ->
            Icon(
                Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                tint = color.copy(alpha = (0.3f + 0.35f * (2 - index) / 2f) * alpha),
                modifier = Modifier.size(22.dp).graphicsLayer {
                    translationY = -4.dp.toPx() * index
                },
            )
        }
    }
}

/**
 * 可上滑触发的大圆形按钮。
 * 拖动时直接跟手（snapTo），松手未达阈值则弹簧回弹（animateTo）。
 */
@Composable
internal fun SlideCallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    label: String,
    onClick: () -> Unit,
) {
    val triggerThreshold = with(LocalDensity.current) { -140.dp.toPx() }
    val animOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var dragging by remember { mutableStateOf(false) }
    // 防抖：防止拖动到阈值触发后，clickable 的 onClick 也重复触发
    var triggered by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // chevronAlpha 在 graphicsLayer 中读取 animOffset.value（draw 阶段），
        // 避免每次拖动帧触发 SlideCallButton 整体重组
        Box(
            modifier = Modifier.graphicsLayer {
                val progress = (-animOffset.value / triggerThreshold).coerceIn(0f, 1f)
                alpha = 1f - progress
            }
        ) {
            TripleChevron(color = backgroundColor)
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer { translationY = animOffset.value }
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = { if (!triggered) { triggered = true; onClick() } })
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            dragging = true
                            triggered = false
                            scope.launch { animOffset.snapTo(0f) }
                        },
                        onDragEnd = {
                            dragging = false
                            // 未达阈值：柔和弹簧回弹
                            scope.launch {
                                animOffset.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                                )
                            }
                        },
                        onDragCancel = {
                            dragging = false
                            scope.launch {
                                animOffset.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                                )
                            }
                        },
                    ) { _, deltaY ->
                        if (!dragging) return@detectVerticalDragGestures
                        val newOffset = (animOffset.value + deltaY).coerceAtMost(0f)
                        // 跟手：立即更新，无动画
                        scope.launch { animOffset.snapTo(newOffset) }
                        // 达到阈值触发
                        if (newOffset <= triggerThreshold) {
                            dragging = false
                            if (!triggered) { triggered = true; onClick() }
                            scope.launch { animOffset.snapTo(0f) }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}
