package com.muchen.virtualcall.presentation.call

import androidx.compose.runtime.Immutable
import com.muchen.virtualcall.domain.model.Caller

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
