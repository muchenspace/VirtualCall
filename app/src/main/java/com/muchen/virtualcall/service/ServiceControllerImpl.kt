package com.muchen.virtualcall.service

import android.content.Context
import android.content.Intent
import com.muchen.virtualcall.domain.service.ServiceController
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ServiceController {

    override fun ensureRunning() {
        startServiceWithAction(ServiceActions.ACTION_ENSURE_RUNNING)
    }

    override fun refreshNotification() {
        startServiceWithAction(ServiceActions.ACTION_REFRESH_NOTIFICATION)
    }

    override fun triggerTestCall() {
        startServiceWithAction(ServiceActions.ACTION_TRIGGER_TEST_CALL)
    }

    override fun notifyCallAnswered() {
        sendActionToRunningService(ServiceActions.ACTION_ANSWER_CALL)
    }

    override fun notifyCallDismissed() {
        sendActionToRunningService(ServiceActions.ACTION_DISMISS_CALL)
    }

    override fun stopService() {
        startServiceWithAction(ServiceActions.ACTION_STOP_SERVICE)
    }

    override fun hideOverlay() {
        runCatching {
            context.stopService(Intent(context, IncomingCallOverlayService::class.java))
        }
    }

    private fun startServiceWithAction(action: String) {
        val intent = Intent(context, VirtualCallService::class.java).apply {
            this.action = action
        }
        ServiceStarter.safeStartForegroundService(context, intent)
    }

    private fun sendActionToRunningService(action: String) {
        val intent = Intent(context, VirtualCallService::class.java).apply {
            this.action = action
        }
        ServiceStarter.safeStartService(context, intent)
    }
}
