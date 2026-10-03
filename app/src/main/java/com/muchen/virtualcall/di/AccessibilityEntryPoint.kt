package com.muchen.virtualcall.di

import com.muchen.virtualcall.domain.repository.SystemRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AccessibilityEntryPoint {
    fun systemRepository(): SystemRepository
}
