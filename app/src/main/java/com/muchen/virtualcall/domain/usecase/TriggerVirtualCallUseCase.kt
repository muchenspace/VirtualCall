package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import javax.inject.Inject

class TriggerVirtualCallUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
    private val virtualCallRepository: VirtualCallRepository,
    private val settingsRepository: SettingsRepository,
) {

    sealed class Result {
        data object NotArmed : Result()
        data object AlreadyRinging : Result()
        data object BackgroundPopupNotGranted : Result()
        data class Triggered(val mode: PresentationMode) : Result()
    }

    operator fun invoke(bypassArmed: Boolean = false): Result {
        if (!systemRepository.isBackgroundPopupGranted()) return Result.BackgroundPopupNotGranted
        if (!bypassArmed && (!systemRepository.isServiceArmed() || systemRepository.isUserDisarmed())) {
            return Result.NotArmed
        }
        if (!virtualCallRepository.tryMarkRinging()) return Result.AlreadyRinging
        val mode = settingsRepository.getPresentationMode()
        return Result.Triggered(mode)
    }
}
