package com.muchen.virtualcall.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.muchen.virtualcall.VirtualCallActivity
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.service.notification.CallNotificationHelper
import com.muchen.virtualcall.service.recovery.ServiceRecoveryHelper
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VirtualCallService : Service() {

    @Inject lateinit var virtualCallRepository: VirtualCallRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var systemRepository: SystemRepository
    @Inject lateinit var notificationHelper: CallNotificationHelper
    @Inject lateinit var recoveryHelper: ServiceRecoveryHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
        recoveryHelper.cancelScheduledRestart()
        recoveryHelper.markRunning()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        if (intent?.action != ServiceActions.ACTION_STOP_SERVICE) {
            recoveryHelper.cancelScheduledRestart()
        }
        startForegroundSafely()
        when (intent?.action) {
            ServiceActions.ACTION_TRIGGER_CALL -> triggerVirtualCall(returnToApp = false)
            ServiceActions.ACTION_TRIGGER_TEST_CALL -> triggerVirtualCall(returnToApp = true)
            ServiceActions.ACTION_ANSWER_CALL -> virtualCallRepository.markInCall()
            ServiceActions.ACTION_SHOW_FULLSCREEN_CALL -> showFullscreenCall(forceInCall = virtualCallRepository.isInCall())
            ServiceActions.ACTION_DISMISS_CALL -> dismissVirtualCall()
            ServiceActions.ACTION_STOP_SERVICE -> stopServiceExplicitly()
            ServiceActions.ACTION_REFRESH_NOTIFICATION -> refreshNotification()
            ServiceActions.ACTION_ENSURE_RUNNING, null -> ensureRunning()
            else -> Unit
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Task removed; attempting immediate recovery")
        recoveryHelper.requestImmediateRecovery("task_removed")
        recoveryHelper.scheduleRestart("task_removed")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        if (!recoveryHelper.isStoppingExplicitly) {
            Log.d(TAG, "Service destroyed unexpectedly; scheduling recovery")
            recoveryHelper.requestImmediateRecovery("service_destroyed")
            recoveryHelper.scheduleRestart("service_destroyed")
        } else {
            Log.d(TAG, "Service destroyed after explicit stop")
            recoveryHelper.cancelScheduledRestart()
        }
        super.onDestroy()
    }

    private fun startForegroundSafely() {
        val notification = notificationHelper.buildServiceNotification()
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(CallNotificationHelper.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(CallNotificationHelper.NOTIFICATION_ID, notification)
            }
        }.onFailure { Log.e(TAG, "startForeground failed", it) }
    }

    private fun ensureRunning() {
        recoveryHelper.markRunning()
        recoveryHelper.cancelScheduledRestart()
        // 启动后根据当前无障碍/武装状态刷新通知文案
        notificationHelper.refreshServiceNotification()
    }

    /** 刷新常驻通知文案（无障碍或武装状态变化时调用）。 */
    private fun refreshNotification() {
        notificationHelper.refreshServiceNotification()
    }

    private fun triggerVirtualCall(returnToApp: Boolean) {
        val isArmed = systemRepository.isServiceArmed()
        if (!isArmed) {
            // 测试触发无视武装状态，方便用户在 App 内预览
            if (!returnToApp) {
                Log.d(TAG, "Trigger ignored because service is not armed")
                return
            }
        }
        if (!virtualCallRepository.tryMarkRinging()) {
            Log.d(TAG, "Ignoring trigger because virtual call UI is already active")
            return
        }
        val mode = settingsRepository.getPresentationMode()
        if (mode == com.muchen.virtualcall.domain.model.PresentationMode.OVERLAY && Settings.canDrawOverlays(this)) {
            ServiceStarter.safeStartService(this, Intent(this, IncomingCallOverlayService::class.java))
            // overlay 模式下若用户已接听升级到全屏，挂断时无需回 App（用户此时不在 App 内）
        } else {
            showFullscreenCall(forceInCall = false, returnToApp = returnToApp)
        }
    }

    private fun showFullscreenCall(forceInCall: Boolean, returnToApp: Boolean = false) {
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        val started = runCatching { startCallActivity(forceInCall, returnToApp); true }
            .onFailure { Log.e(TAG, "Direct launch failed", it) }
            .getOrDefault(false)
        if (!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notificationHelper.showFullScreenCallNotification(
                callerName = settingsRepository.getCaller().name,
                forceInCall = forceInCall,
            )
        }
    }

    private fun startCallActivity(forceInCall: Boolean, returnToApp: Boolean = false) {
        val intent = Intent(this, VirtualCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(VirtualCallActivity.EXTRA_START_IN_CALL, forceInCall)
            putExtra(VirtualCallActivity.EXTRA_RETURN_TO_APP, returnToApp)
        }
        startActivity(intent)
    }

    private fun dismissVirtualCall() {
        virtualCallRepository.clear()
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        notificationHelper.cancelCallNotification()
    }

    private fun stopServiceExplicitly() {
        Log.d(TAG, "Disarming service (user stop)")
        // 不停止前台服务进程：通知栏需持续显示「服务未开启」状态。
        // 仅解除武装，刷新通知文案。
        virtualCallRepository.clear()
        systemRepository.setServiceArmed(false)
        notificationHelper.refreshServiceNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "VirtualCallService"
    }
}
