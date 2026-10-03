package com.muchen.virtualcall.data.local

import com.muchen.virtualcall.domain.model.CallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualCallSession @Inject constructor() {

    private val _state = MutableStateFlow(CallState.IDLE)
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val lock = Any()

    fun tryMarkRinging(): Boolean = synchronized(lock) {
        if (_state.value != CallState.IDLE) return false
        _state.value = CallState.RINGING
        true
    }

    fun markInCall() = synchronized(lock) {
        _state.value = CallState.INCALL
    }

    fun clear() = synchronized(lock) {
        _state.value = CallState.IDLE
    }

    fun isInCall(): Boolean = _state.value == CallState.INCALL

    fun isRinging(): Boolean = _state.value == CallState.RINGING

    fun getState(): CallState = _state.value
}
