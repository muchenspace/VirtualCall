package com.muchen.virtualcall.presentation.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.ui.theme.CallDeclineRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 顶部悬浮窗来电卡片。
 * 胶囊形渐变背景（左深右绿），左侧姓名+位置，右侧接听/挂断按钮。
 * 带渐入（顶部滑入+渐显）和渐出（淡出+上移）动画。
 */
@Composable
fun IncomingCallOverlay(
    caller: Caller,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onCardClick: () -> Unit,
) {
    val density = LocalDensity.current
    val slideDistancePx = with(density) { 120.dp.toPx() }

    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF1C1C1E),
                Color(0xFF1C1C1E),
                Color(0xFF1A6B4A),
                Color(0xFF2D8B5E),
            ),
            startX = 0f,
            endX = Float.POSITIVE_INFINITY,
        )
    }

    var entered by remember { mutableStateOf(false) }
    var exiting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { entered = true }

    val animSpec = tween<Float>(durationMillis = 360, easing = FastOutSlowInEasing)
    val alphaState = animateFloatAsState(
        targetValue = if (entered && !exiting) 1f else 0f,
        animationSpec = animSpec,
        label = "overlayAlpha",
    )
    val slideProgressState = animateFloatAsState(
        targetValue = if (entered && !exiting) 0f else 1f,
        animationSpec = animSpec,
        label = "overlaySlide",
    )

    fun exitThen(action: () -> Unit) {
        if (exiting) return
        exiting = true
        scope.launch {
            try {
                delay(360)
            } finally {
                action()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alphaState.value
                this.translationY = -slideDistancePx * slideProgressState.value
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(gradientBrush)
                .clickable(onClick = { exitThen(onCardClick) })
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = caller.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.size(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.SimCard,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = caller.carrier,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverlayCircleButton(
                    backgroundColor = CallDeclineRed,
                    icon = Icons.Filled.CallEnd,
                    onClick = { exitThen(onDecline) },
                )
                OverlayCircleButton(
                    backgroundColor = Color(0xFF34C759),
                    icon = Icons.Filled.Call,
                    onClick = { exitThen(onAnswer) },
                )
            }
        }
    }
}

@Composable
private fun OverlayCircleButton(
    backgroundColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}
