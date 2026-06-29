package com.muchen.virtualcall.data.local

import com.muchen.virtualcall.domain.model.CallState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 线程安全的来电状态机（内存数据源）。
 * 从原 object VirtualCallSession 重构为 @Singleton class，通过 Hilt 注入。
 * 保持 synchronized + @Volatile 线程安全语义不变。
 */
@Singleton
class VirtualCallSession @Inject constructor() {

    @Volatile
    private var state: CallState = CallState.IDLE

    private val lock = Any()

    /**
     * 原子操作：仅在当前状态为 IDLE 时切换为 RINGING。
     * 防止两个并发触发同时通过 IDLE 检查并重复标记 ringing。
     */
    fun tryMarkRinging(): Boolean {
        synchronized(lock) {
            if (state != CallState.IDLE) return false
            state = CallState.RINGING
            return true
        }
    }

    fun markInCall() {
        synchronized(lock) {
            state = CallState.INCALL
        }
    }

    fun clear() {
        synchronized(lock) {
            state = CallState.IDLE
        }
    }

    fun isInCall(): Boolean = state == CallState.INCALL

    fun isRinging(): Boolean = state == CallState.RINGING

    fun getState(): CallState = state
}
