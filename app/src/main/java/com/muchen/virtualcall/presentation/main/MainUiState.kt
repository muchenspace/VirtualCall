package com.muchen.virtualcall.presentation.main

import androidx.compose.runtime.Immutable
import com.muchen.virtualcall.domain.model.PresentationMode

@Immutable
data class MainUiState(
    val contactName: String = "",
    val contactNumber: String = "",
    val contactCarrier: String = "",
    val accessibilityEnabled: Boolean = false,
    val serviceArmed: Boolean = false,
    val isBackgroundPopupGranted: Boolean = true,
    val presentationMode: PresentationMode = PresentationMode.FULLSCREEN,
    val overlayPermissionGranted: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
    val fullScreenIntentGranted: Boolean = false,
    val exactAlarmAllowed: Boolean = false,
    val ringtoneLabel: String = "",
    val hasCustomRingtone: Boolean = false,
    val recordingLabel: String = "",
    val hasRecording: Boolean = false,
    val presentationStatusText: String = "",
)
