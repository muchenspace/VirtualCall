package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 保存来电展示模式。
 */
class SavePresentationModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(mode: PresentationMode) {
        settingsRepository.savePresentationMode(mode)
    }
}
