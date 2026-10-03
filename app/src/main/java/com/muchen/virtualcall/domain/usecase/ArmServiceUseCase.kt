package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

class ArmServiceUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
) {
    operator fun invoke() {
        systemRepository.setUserDisarmed(false)
        systemRepository.setServiceArmed(true)
    }
}
