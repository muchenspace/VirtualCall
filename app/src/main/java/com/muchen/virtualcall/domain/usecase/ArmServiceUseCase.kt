package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

/**
 * 武装虚拟来电服务：设置 service_armed = true。
 * 调用方（Presenter）负责额外启动前台服务。
 */
class ArmServiceUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
) {
    operator fun invoke() {
        systemRepository.setServiceArmed(true)
    }
}
