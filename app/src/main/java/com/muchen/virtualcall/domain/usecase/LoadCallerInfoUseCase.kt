package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

class LoadCallerInfoUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Caller = settingsRepository.getCaller()
}
