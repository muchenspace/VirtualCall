package com.muchen.virtualcall.presentation.call.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.ui.theme.CallChipBg
import com.muchen.virtualcall.ui.theme.CallDeclineRed
import com.muchen.virtualcall.ui.theme.CallPhoneNumber
import com.muchen.virtualcall.ui.theme.CallSecondary
import java.util.Locale

/**
 * 通话中页面：顶部联系人信息 + 底部 3x3 网格操作按钮 + 拨号盘。
 */
@Composable
internal fun InCallContent(
    caller: Caller,
    isConnecting: Boolean,
    callDurationSeconds: Int,
    isMuted: Boolean,
    isOnHold: Boolean,
    isSpeakerOn: Boolean,
    isKeypadVisible: Boolean,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleKeypad: () -> Unit,
    onKeypadDigit: (Char) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 100.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 联系人信息区
        Text(
            text = caller.name,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = caller.formattedNumber,
            color = CallPhoneNumber,
            fontSize = 17.sp,
        )
        Spacer(Modifier.height(12.dp))
        // 呼叫状态：带小手机图标
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(18.dp).clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Call, null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            val statusText = when {
                isOnHold -> stringResource(R.string.call_status_hold)
                isConnecting -> stringResource(R.string.call_status_connecting)
                else -> stringResource(R.string.call_status_connected)
            }
            // 连接后显示通话时长
            val durationText = if (isConnecting) "" else "  ${formatDuration(callDurationSeconds)}"
            Text(
                text = statusText + durationText,
                color = Color.White,
                fontSize = 15.sp,
            )
        }

        Spacer(Modifier.weight(1f))

        // 底部操作区：键盘与按钮网格互相切换，带淡入淡出 + 上下滑动过渡
        AnimatedContent(
            targetState = isKeypadVisible,
            transitionSpec = {
                if (targetState) {
                    // 键盘弹出：键盘从下滑入，按钮网格向上淡出
                    (fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 3 }) togetherWith
                        (fadeOut(tween(180)) + slideOutVertically(tween(280)) { -it / 6 })
                } else {
                    // 键盘收起：按钮网格从上滑入，键盘向下滑出
                    (fadeIn(tween(280)) + slideInVertically(tween(280)) { -it / 6 }) togetherWith
                        (fadeOut(tween(180)) + slideOutVertically(tween(280)) { it / 3 })
                }
            },
            label = "keypadToggle",
        ) { showKeypad ->
            if (showKeypad) {
                // 拨号盘（按键之间透明，背景透出）
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DtmfKeypad(onDigit = onKeypadDigit)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.action_keypad_hide),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(onClick = onToggleKeypad),
                    )
                }
            } else {
                // 3x3 操作按钮网格
                // 提取 stringResource 到局部变量（composable 不能放在 remember 内）
                val sRecord = stringResource(R.string.action_record)
                val sAddCall = stringResource(R.string.action_add_call)
                val sVideo = stringResource(R.string.action_video)
                val sAi = stringResource(R.string.action_ai_summary)
                val sEndCall = stringResource(R.string.action_end_call)
                val sKeypad = stringResource(R.string.action_keypad_hide)
                val sHold = stringResource(if (isOnHold) R.string.action_unhold else R.string.action_hold)
                val sMute = stringResource(if (isMuted) R.string.action_unmute else R.string.action_mute)
                val sSpeaker = stringResource(if (isSpeakerOn) R.string.action_speaker_off else R.string.action_speaker)
                // 记忆化按钮列表：仅在 isOnHold/isMuted/isSpeakerOn 变化时重建，
                // 避免每秒 callDurationSeconds 变化时重建 9 个 GridAction + ImageVector 查找
                val buttons = remember(isOnHold, isMuted, isSpeakerOn, sHold, sMute, sSpeaker) {
                    listOf(
                        GridAction(sRecord, Icons.Outlined.FiberManualRecord, GridActionId.RECORD),
                        GridAction(sHold, if (isOnHold) Icons.Outlined.PlayCircleOutline else Icons.Outlined.PauseCircleOutline, GridActionId.HOLD, isActive = isOnHold),
                        GridAction(sAddCall, Icons.Outlined.PersonAdd, GridActionId.ADD_CALL),
                        GridAction(sMute, if (isMuted) Icons.Outlined.Mic else Icons.Outlined.MicOff, GridActionId.MUTE, isActive = isMuted),
                        GridAction(sVideo, Icons.Outlined.Videocam, GridActionId.VIDEO),
                        GridAction(sAi, Icons.Outlined.AutoAwesome, GridActionId.AI, isGradientBorder = true),
                        GridAction(sSpeaker, if (isSpeakerOn) Icons.AutoMirrored.Outlined.VolumeUp else Icons.Outlined.VolumeOff, GridActionId.SPEAKER, isActive = isSpeakerOn),
                        GridAction(sEndCall, Icons.Filled.CallEnd, GridActionId.END_CALL, isRedBackground = true),
                        GridAction(sKeypad, Icons.Outlined.Dialpad, GridActionId.KEYPAD),
                    )
                }

                // 3x3 操作按钮网格：用 Column+Row 替代 LazyVerticalGrid，
                // 9 个固定项无需懒加载，避免过渡动画期间双重测量开销
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    buttons.chunked(3).forEach { rowButtons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            rowButtons.forEach { action ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    InCallGridButton(
                                        action = action,
                                        onClick = {
                                            when (action.id) {
                                                GridActionId.END_CALL -> onEndCall()
                                                GridActionId.MUTE -> onToggleMute()
                                                GridActionId.HOLD -> onToggleHold()
                                                GridActionId.SPEAKER -> onToggleSpeaker()
                                                GridActionId.KEYPAD -> onToggleKeypad()
                                                // RECORD, AI, ADD_CALL, VIDEO — 无反应
                                                else -> {}
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 按钮功能标识 */
private enum class GridActionId { RECORD, HOLD, ADD_CALL, MUTE, VIDEO, AI, SPEAKER, END_CALL, KEYPAD }

/** 3x3 网格中每个圆形操作按钮的数据 */
@Immutable
private data class GridAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val id: GridActionId,
    val isGradientBorder: Boolean = false,
    val isRedBackground: Boolean = false,
    val isActive: Boolean = false,
)

/** 3x3 网格圆形按钮：半透明底 + 图标 + 文字标签 */
@Composable
private fun InCallGridButton(
    action: GridAction,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.92f else 1f
    // 记忆化 AI 按钮渐变 Brush，避免每次 drawBehind 执行时分配新对象
    val aiGradientBrush = remember {
        Brush.sweepGradient(
            colors = listOf(
                Color(0xFF4FC3F7), Color(0xFFBA68C8),
                Color(0xFFFFB74D), Color(0xFF4FC3F7),
            )
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .then(
                    if (action.isGradientBorder) {
                        Modifier.drawBehind {
                            // AI 按钮：渐变色圆环边框
                            drawCircle(color = CallSecondary, radius = size.minDimension / 2f)
                            drawCircle(
                                brush = aiGradientBrush,
                                radius = size.minDimension / 2f - 2.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    } else if (action.isRedBackground) {
                        Modifier.background(CallDeclineRed)
                    } else if (action.isActive) {
                        // 开启态：纯白背景填充整个按钮
                        Modifier.background(Color.White)
                    } else {
                        Modifier.background(CallChipBg)
                    }
                )
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                action.icon, action.label,
                tint = if (action.isActive) Color.Black else if (action.isRedBackground) Color.White else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = action.label,
            color = if (action.isActive) Color.White else Color.White.copy(alpha = 0.65f),
            fontSize = 11.sp,
        )
    }
}

private fun formatDuration(seconds: Int): String =
    String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
