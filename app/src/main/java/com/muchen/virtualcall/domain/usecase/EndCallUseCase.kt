package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import javax.inject.Inject

/**
 * 结束通话：重置状态机为 IDLE。
 * 调用方（Presenter）负责停止铃声、发送 dismiss 指令、关闭界面。
 */
class EndCallUseCase @Inject constructor(
    private val virtualCallRepository: VirtualCallRepository,
) {
    operator fun invoke() {
        virtualCallRepository.clear()
    }
}
