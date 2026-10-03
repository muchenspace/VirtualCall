package com.muchen.virtualcall.data.repository

import com.muchen.virtualcall.data.local.VirtualCallSession
import com.muchen.virtualcall.domain.model.CallState
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualCallRepositoryImpl @Inject constructor(
    private val session: VirtualCallSession,
) : VirtualCallRepository {

    override val state: StateFlow<CallState> = session.state

    override fun tryMarkRinging(): Boolean = session.tryMarkRinging()

    override fun markInCall() = session.markInCall()

    override fun clear() = session.clear()

    override fun isInCall(): Boolean = session.isInCall()

    override fun isRinging(): Boolean = session.isRinging()

    override fun getState(): CallState = session.getState()
}
