package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import javax.inject.Inject

class EndCallUseCase @Inject constructor(
    private val virtualCallRepository: VirtualCallRepository,
) {
    operator fun invoke() {
        virtualCallRepository.clear()
    }
}
