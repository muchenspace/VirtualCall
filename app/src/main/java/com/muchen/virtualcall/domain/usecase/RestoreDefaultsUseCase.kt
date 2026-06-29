package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 恢复所有用户配置为默认值（联系人、号码、展示模式、铃声）。
 */
class RestoreDefaultsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke() {
        settingsRepository.restoreDefaults()
    }
}
