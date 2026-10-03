package com.muchen.virtualcall.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.muchen.virtualcall.VirtualCallActivity
import com.muchen.virtualcall.domain.model.CallState
import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.usecase.TriggerVirtualCallUseCase
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
    @Inject lateinit var triggerVirtualCallUseCase: TriggerVirtualCallUseCase

    private val handler = Handler(Looper.getMainLooper())
    private val ringTimeoutRunnable = Runnable {
        if (virtualCallRepository.getState() == CallState.RINGING) {
            Log.d(TAG, "Ring timeout reached; dismissing virtual call")
            dismissVirtualCall()
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
        recoveryHelper.cancelScheduledRestart()
        recoveryHelper.scheduleWatchdog()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        if (intent?.action == ServiceActions.ACTION_STOP_SERVICE) {
            stopServiceExplicitly()
            return START_NOT_STICKY
        }
        if (intent?.action == null && !recoveryHelper.shouldRecover()) {
            Log.d(TAG, "Sticky restart ignored because service is not armed")
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ServiceActions.ACTION_ENSURE_RUNNING && !recoveryHelper.shouldRecover()) {
            Log.d(TAG, "Ensure-running ignored because service is not armed")
            stopSelf()
            return START_NOT_STICKY
        }
        recoveryHelper.cancelScheduledRestart()
        if (!startForegroundSafely()) {
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.action) {
            ServiceActions.ACTION_TRIGGER_CALL -> triggerVirtualCall(returnToApp = false)
            ServiceActions.ACTION_TRIGGER_TEST_CALL -> triggerVirtualCall(returnToApp = true)
            ServiceActions.ACTION_ANSWER_CALL -> {
                virtualCallRepository.markInCall()
                handler.removeCallbacks(ringTimeoutRunnable)
                notificationHelper.cancelCallNotification()
            }
            ServiceActions.ACTION_SHOW_FULLSCREEN_CALL -> showFullscreenCall(forceInCall = virtualCallRepository.isInCall())
            ServiceActions.ACTION_DISMISS_CALL -> dismissVirtualCall()
            ServiceActions.ACTION_REFRESH_NOTIFICATION -> refreshNotification()
            ServiceActions.ACTION_ENSURE_RUNNING, null -> ensureRunning()
            else -> Unit
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Task removed; attempting recovery")
        recoveryHelper.requestImmediateRecovery("task_removed")
        recoveryHelper.scheduleRestart("task_removed")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(ringTimeoutRunnable)
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        if (recoveryHelper.shouldRecover()) {
            Log.d(TAG, "Service destroyed unexpectedly; scheduling recovery")
            recoveryHelper.scheduleRestart("service_destroyed")
        } else {
            Log.d(TAG, "Service destroyed after explicit stop")
            recoveryHelper.cancelScheduledRestart()
            recoveryHelper.cancelWatchdog()
        }
        super.onDestroy()
    }

    private fun startForegroundSafely(): Boolean {
        val notification = notificationHelper.buildServiceNotification()
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(CallNotificationHelper.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(CallNotificationHelper.NOTIFICATION_ID, notification)
            }
            true
        }.onFailure { Log.e(TAG, "startForeground failed", it) }.getOrDefault(false)
    }

    private fun ensureRunning() {
        recoveryHelper.cancelScheduledRestart()
        recoveryHelper.scheduleWatchdog()
        notificationHelper.refreshServiceNotification()
    }

    private fun refreshNotification() {
        notificationHelper.refreshServiceNotification()
    }

    private fun triggerVirtualCall(returnToApp: Boolean) {
        val result = triggerVirtualCallUseCase(bypassArmed = returnToApp)
        when (result) {
            is TriggerVirtualCallUseCase.Result.NotArmed -> {
                Log.d(TAG, "Trigger ignored because service is not armed")
                return
            }
            is TriggerVirtualCallUseCase.Result.AlreadyRinging -> {
                Log.d(TAG, "Ignoring trigger because virtual call UI is already active")
                return
            }
            is TriggerVirtualCallUseCase.Result.BackgroundPopupNotGranted -> {
                Log.d(TAG, "Trigger ignored because background popup permission is not granted")
                notificationHelper.cancelCallNotification()
                return
            }
            is TriggerVirtualCallUseCase.Result.Triggered -> Unit
        }
        handler.removeCallbacks(ringTimeoutRunnable)
        handler.postDelayed(ringTimeoutRunnable, RING_TIMEOUT_MS)
        val mode = (result as TriggerVirtualCallUseCase.Result.Triggered).mode
        if (mode == PresentationMode.OVERLAY && Settings.canDrawOverlays(this)) {
            ServiceStarter.safeStartForegroundService(this, Intent(this, IncomingCallOverlayService::class.java))
        } else {
            showFullscreenCall(forceInCall = false, returnToApp = returnToApp)
        }
    }

    private fun showFullscreenCall(forceInCall: Boolean, returnToApp: Boolean = false) {
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        val caller = settingsRepository.getCaller()
        notificationHelper.showFullScreenCallNotification(
            callerName = caller.name,
            forceInCall = forceInCall,
        )
        runCatching { startCallActivity(forceInCall, returnToApp) }
            .onFailure { Log.e(TAG, "Direct launch failed", it) }
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
        handler.removeCallbacks(ringTimeoutRunnable)
        virtualCallRepository.clear()
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        notificationHelper.cancelCallNotification()
    }

    private fun stopServiceExplicitly() {
        Log.d(TAG, "Disarming service (user stop)")
        handler.removeCallbacks(ringTimeoutRunnable)
        systemRepository.setServiceArmed(false)
        systemRepository.setUserDisarmed(true)
        virtualCallRepository.clear()
        runCatching { stopService(Intent(this, IncomingCallOverlayService::class.java)) }
        notificationHelper.cancelCallNotification()
        recoveryHelper.cancelScheduledRestart()
        recoveryHelper.cancelWatchdog()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "VirtualCallService"
        private const val RING_TIMEOUT_MS = 45_000L
    }
}
