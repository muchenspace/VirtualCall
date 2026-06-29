package com.muchen.virtualcall.di

import com.muchen.virtualcall.data.repository.VirtualCallRepositoryImpl
import com.muchen.virtualcall.data.repository.SettingsRepositoryImpl
import com.muchen.virtualcall.data.repository.SystemRepositoryImpl
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.domain.service.RecordingPlayer
import com.muchen.virtualcall.domain.service.ServiceController
import com.muchen.virtualcall.service.RecordingPlayerImpl
import com.muchen.virtualcall.service.ServiceControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI 模块：绑定 domain 层接口到 data/service 层实现。
 * PrefsDataSource、VirtualCallSession 因标注 @Singleton + @Inject constructor，
 * Hilt 自动提供，无需显式 @Provides。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindVirtualCallRepository(impl: VirtualCallRepositoryImpl): VirtualCallRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSystemRepository(impl: SystemRepositoryImpl): SystemRepository

    @Binds
    @Singleton
    abstract fun bindServiceController(impl: ServiceControllerImpl): ServiceController

    @Binds
    abstract fun bindRecordingPlayer(impl: RecordingPlayerImpl): RecordingPlayer
}
