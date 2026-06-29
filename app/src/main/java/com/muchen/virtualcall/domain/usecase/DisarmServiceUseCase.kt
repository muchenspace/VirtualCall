package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

/**
 * 解除虚拟来电服务武装：设置 service_armed = false。
 * 调用方（Presenter）负责额外发送停止服务指令。
 */
class DisarmServiceUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
) {
    operator fun invoke() {
        systemRepository.setServiceArmed(false)
    }
}
