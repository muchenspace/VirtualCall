package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.ServiceStatus
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

/**
 * 获取服务综合状态：armed + accessibility + overlay + presentation mode + ringtone URI。
 * 调用方应在 IO 线程执行（Settings.Secure 查询为同步 binder IPC）。
 */
class GetServiceStatusUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): ServiceStatus = ServiceStatus(
        isArmed = systemRepository.isServiceArmed(),
        isAccessibilityEnabled = systemRepository.isAccessibilityEnabled(),
        isOverlayPermissionGranted = systemRepository.isOverlayPermissionGranted(),
        isIgnoringBatteryOptimizations = systemRepository.isIgnoringBatteryOptimizations(),
        presentationMode = settingsRepository.getPresentationMode(),
        customRingtoneUri = settingsRepository.getCustomRingtoneUri(),
        recordingUri = settingsRepository.getRecordingUri(),
    )
}
