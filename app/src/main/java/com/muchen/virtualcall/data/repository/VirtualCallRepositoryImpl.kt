package com.muchen.virtualcall.data.repository

import com.muchen.virtualcall.data.local.VirtualCallSession
import com.muchen.virtualcall.domain.model.CallState
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VirtualCallRepository 实现：委托给 [VirtualCallSession] 状态机。
 */
@Singleton
class VirtualCallRepositoryImpl @Inject constructor(
    private val session: VirtualCallSession,
) : VirtualCallRepository {

    override fun tryMarkRinging(): Boolean = session.tryMarkRinging()

    override fun markInCall() = session.markInCall()

    override fun clear() = session.clear()

    override fun isInCall(): Boolean = session.isInCall()

    override fun isRinging(): Boolean = session.isRinging()

    override fun getState(): CallState = session.getState()
}
