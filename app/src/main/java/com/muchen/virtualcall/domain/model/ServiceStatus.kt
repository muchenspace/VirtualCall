package com.muchen.virtualcall.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * 服务综合状态快照（原始数据，不含展示字符串）。
 * Presenter 负责将此映射为 UiState 并构建展示文本。
 */
@Immutable
data class ServiceStatus(
    val isArmed: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val presentationMode: PresentationMode = PresentationMode.FULLSCREEN,
    val customRingtoneUri: Uri? = null,
    val recordingUri: Uri? = null,
)
