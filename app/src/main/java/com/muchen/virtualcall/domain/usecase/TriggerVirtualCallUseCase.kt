package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import javax.inject.Inject

/**
 * 触发虚拟来电的 Domain 逻辑：
 * 1. 检查服务是否已武装
 * 2. 原子操作 tryMarkRinging 防止并发触发
 * 3. 返回触发结果，由调用方（Service）执行 Android 组件启动
 */
class TriggerVirtualCallUseCase @Inject constructor(
    private val systemRepository: SystemRepository,
    private val virtualCallRepository: VirtualCallRepository,
    private val virtualCallSettingsRepository: com.muchen.virtualcall.domain.repository.SettingsRepository,
) {

    sealed class Result {
        object NotArmed : Result()
        object AlreadyRinging : Result()
        data class Triggered(val mode: PresentationMode) : Result()
    }

    operator fun invoke(): Result {
        if (!systemRepository.isServiceArmed()) return Result.NotArmed
        if (!virtualCallRepository.tryMarkRinging()) return Result.AlreadyRinging
        val mode = virtualCallSettingsRepository.getPresentationMode()
        return Result.Triggered(mode)
    }
}
