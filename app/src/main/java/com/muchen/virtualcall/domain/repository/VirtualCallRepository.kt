package com.muchen.virtualcall.domain.repository

import com.muchen.virtualcall.domain.model.CallState

/**
 * 虚拟来电状态机 Repository 契约。
 * 封装 VirtualCallSession 的原子状态转换操作。
 */
interface VirtualCallRepository {

    /**
     * 原子操作：仅在当前状态为 IDLE 时切换为 RINGING。
     * 解决 canStartIncomingCall() + markRinging() 两步操作间的竞态条件。
     * @return true 如果成功切换到 RINGING，false 如果已有来电进行中。
     */
    fun tryMarkRinging(): Boolean

    /** 标记进入通话中状态。 */
    fun markInCall()

    /** 重置为 IDLE 状态。 */
    fun clear()

    /** 当前是否处于通话中。 */
    fun isInCall(): Boolean

    /** 当前是否处于响铃中。 */
    fun isRinging(): Boolean

    /** 获取当前状态。 */
    fun getState(): CallState
}
