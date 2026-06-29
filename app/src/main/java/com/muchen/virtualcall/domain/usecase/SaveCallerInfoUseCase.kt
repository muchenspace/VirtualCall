package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 保存联系人姓名、号码和运营商。
 */
class SaveCallerInfoUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(name: String, number: String, carrier: String) {
        settingsRepository.saveCaller(name, number, carrier)
    }
}
