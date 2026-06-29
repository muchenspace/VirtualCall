package com.muchen.virtualcall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.ui.theme.LocalAppTheme

/**
 * 分组卡片：图标标题 + 副标题 + 内容。
 */
@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppTheme.current
    val resolvedIconTint = if (iconTint == Color.Unspecified) colors.accent else iconTint
    GlassCard {
        if (title.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(resolvedIconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = resolvedIconTint, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, color = colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    if (subtitle != null) {
                        Spacer(Modifier.height(3.dp))
                        Text(subtitle, color = colors.textSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        content()
    }
}
