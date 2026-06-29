package com.muchen.virtualcall.presentation.call

import androidx.compose.runtime.Immutable
import com.muchen.virtualcall.domain.model.Caller

/**
 * 来电/通话界面 UI 状态。
 * 状态拆分为独立字段，每秒变化的 callDurationSeconds 仅影响通话时长文本，
 * 不会触发整体重组（按钮列表已 remember 化）。
 */
@Immutable
data class VirtualCallUiState(
    val caller: Caller = Caller("", "", null, com.muchen.virtualcall.domain.model.PresentationMode.FULLSCREEN),
    val isAccepted: Boolean = false,
    val isConnecting: Boolean = false,
    val callDurationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isOnHold: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isKeypadVisible: Boolean = false,
)
