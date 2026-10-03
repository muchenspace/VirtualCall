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

val LocalScrolling = compositionLocalOf { false }

@Composable
fun PulsingDot(
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppTheme.current
    val resolvedColor = if (color == Color.Unspecified) colors.statusOnline else color
    val isScrolling = LocalScrolling.current

    val progress = remember { Animatable(0.5f) }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {

            progress.snapTo(0.5f)
        } else {

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
