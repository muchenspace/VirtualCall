package com.muchen.virtualcall.presentation.call

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.presentation.call.components.InCallContent
import com.muchen.virtualcall.presentation.call.components.IncomingContent
import com.muchen.virtualcall.ui.theme.CallBgBottom
import com.muchen.virtualcall.ui.theme.CallBgTop

@Composable
fun VirtualCallScreen(
    caller: Caller,
    isAccepted: Boolean,
    isConnecting: Boolean,
    callDurationSeconds: Int,
    isMuted: Boolean,
    isOnHold: Boolean,
    isSpeakerOn: Boolean,
    isKeypadVisible: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleKeypad: () -> Unit,
    onKeypadDigit: (Char) -> Unit,
) {

    val bgBrush = remember { Brush.verticalGradient(listOf(CallBgTop, CallBgBottom)) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
    ) {

        AnimatedContent(
            targetState = isAccepted,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "callState",
        ) { accepted ->
            if (accepted) {
                InCallContent(
                    caller = caller,
                    isConnecting = isConnecting,
                    callDurationSeconds = callDurationSeconds,
                    isMuted = isMuted,
                    isOnHold = isOnHold,
                    isSpeakerOn = isSpeakerOn,
                    isKeypadVisible = isKeypadVisible,
                    onEndCall = onEndCall,
                    onToggleMute = onToggleMute,
                    onToggleHold = onToggleHold,
                    onToggleSpeaker = onToggleSpeaker,
                    onToggleKeypad = onToggleKeypad,
                    onKeypadDigit = onKeypadDigit,
                )
            } else {
                IncomingContent(
                    caller = caller,
                    onAnswer = onAnswer,
                    onDecline = onDecline,
                )
            }
        }
    }
}
