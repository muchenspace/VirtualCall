package com.muchen.virtualcall.domain.repository

import com.muchen.virtualcall.domain.model.CallState
import kotlinx.coroutines.flow.StateFlow

interface VirtualCallRepository {

    val state: StateFlow<CallState>

    fun tryMarkRinging(): Boolean

    fun markInCall()

    fun clear()

    fun isInCall(): Boolean

    fun isRinging(): Boolean

    fun getState(): CallState
}
