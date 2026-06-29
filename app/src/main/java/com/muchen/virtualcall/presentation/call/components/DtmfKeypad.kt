package com.muchen.virtualcall.presentation.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** DTMF 拨号盘按键布局（常量，避免每次组合重复分配） */
private val DtmfKeys = listOf(
    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
    listOf("*" to "", "0" to "+", "#" to ""),
)

/** DTMF 拨号盘 */
@Composable
internal fun DtmfKeypad(onDigit: (Char) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DtmfKeys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                row.forEach { (digit, sub) ->
                    DtmfKey(digit = digit, sub = sub, onClick = { onDigit(digit.first()) })
                }
            }
        }
    }
}

/** DTMF 单个按键 */
@Composable
private fun DtmfKey(digit: String, sub: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.9f else 1f
    Column(
        modifier = Modifier
            .size(76.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(if (pressed) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(digit, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Medium)
        if (sub.isNotEmpty()) {
            Text(sub, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
        }
    }
}
