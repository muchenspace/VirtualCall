package com.muchen.virtualcall.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class ServiceStatus(
    val isArmed: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isBackgroundPopupGranted: Boolean = true,
    val isOverlayPermissionGranted: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val presentationMode: PresentationMode = PresentationMode.FULLSCREEN,
    val customRingtoneUri: Uri? = null,
    val recordingUri: Uri? = null,
    val isFullScreenIntentGranted: Boolean = true,
    val isExactAlarmAllowed: Boolean = true,
)
