package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.ServiceStatus
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

class GetServiceStatusUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): ServiceStatus = ServiceStatus(
        isArmed = systemRepository.isServiceArmed(),
        isAccessibilityEnabled = systemRepository.isAccessibilityEnabled(),
        isBackgroundPopupGranted = systemRepository.isBackgroundPopupGranted(),
        isOverlayPermissionGranted = systemRepository.isOverlayPermissionGranted(),
        isIgnoringBatteryOptimizations = systemRepository.isIgnoringBatteryOptimizations(),
        presentationMode = settingsRepository.getPresentationMode(),
        customRingtoneUri = settingsRepository.getCustomRingtoneUri(),
        recordingUri = settingsRepository.getRecordingUri(),
        isFullScreenIntentGranted = systemRepository.isFullScreenIntentGranted(),
        isExactAlarmAllowed = systemRepository.isExactAlarmAllowed(),
    )
}
