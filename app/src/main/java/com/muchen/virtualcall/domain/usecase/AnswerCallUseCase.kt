package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import javax.inject.Inject

class AnswerCallUseCase @Inject constructor(
    private val virtualCallRepository: VirtualCallRepository,
) {
    operator fun invoke() {
        virtualCallRepository.markInCall()
    }
}
