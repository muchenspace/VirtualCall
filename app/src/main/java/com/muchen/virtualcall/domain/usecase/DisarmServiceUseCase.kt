package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

class DisarmServiceUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
) {
    operator fun invoke() {
        systemRepository.setServiceArmed(false)
        systemRepository.setUserDisarmed(true)
    }
}
