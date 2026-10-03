package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

class SavePresentationModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(mode: PresentationMode) {
        settingsRepository.savePresentationMode(mode)
    }
}
