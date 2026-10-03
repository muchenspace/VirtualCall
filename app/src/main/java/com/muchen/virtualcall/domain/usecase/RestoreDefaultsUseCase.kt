package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

class RestoreDefaultsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke() {
        settingsRepository.restoreDefaults()
    }
}
