package com.muchen.virtualcall.di

import com.muchen.virtualcall.domain.repository.SystemRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * AccessibilityService 无法使用 @AndroidEntryPoint（系统创建，非 Hilt 管理），
 * 通过 EntryPointAccessors 获取 DI 图中的依赖。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AccessibilityEntryPoint {
    fun systemRepository(): SystemRepository
}
