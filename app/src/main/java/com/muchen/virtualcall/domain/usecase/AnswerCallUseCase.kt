package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import javax.inject.Inject

/**
 * 接听来电：标记状态为 INCALL。
 * 调用方（Presenter）负责延迟接通动画、停止铃声、启动计时器。
 */
class AnswerCallUseCase @Inject constructor(
    private val virtualCallRepository: VirtualCallRepository,
) {
    operator fun invoke() {
        virtualCallRepository.markInCall()
    }
}
