package com.muchen.virtualcall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

enum class BadgeType { SUCCESS, WARNING, ERROR, INFO }

/**
 * 状态徽章：圆点 + 文字。
 */
@Composable
fun StatusBadge(
    text: String,
    type: BadgeType,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppTheme.current
    val (dotColor, textColor, bgColor) = when (type) {
        BadgeType.SUCCESS -> Triple(colors.statusOnline, colors.statusOnline, colors.statusOnline.copy(alpha = 0.12f))
        BadgeType.WARNING -> Triple(colors.statusWarning, colors.statusWarning, colors.statusWarning.copy(alpha = 0.12f))
        BadgeType.ERROR -> Triple(colors.statusOffline, colors.statusOffline, colors.statusOffline.copy(alpha = 0.12f))
        BadgeType.INFO -> Triple(colors.statusInfo, colors.statusInfo, colors.statusInfo.copy(alpha = 0.12f))
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(6.dp))
        Text(text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
