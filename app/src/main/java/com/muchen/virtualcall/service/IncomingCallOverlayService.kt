package com.muchen.virtualcall.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.muchen.virtualcall.VirtualCallActivity
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.presentation.overlay.IncomingCallOverlay
import com.muchen.virtualcall.ui.theme.OverlayTheme
import com.muchen.virtualcall.util.RingtonePlayer
import com.muchen.virtualcall.util.ServiceComposeOwner
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IncomingCallOverlayService : Service() {

    @Inject lateinit var virtualCallRepository: VirtualCallRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var composeOwner: ServiceComposeOwner? = null
    private val ringtonePlayer = RingtonePlayer(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (overlayView == null) {
            showOverlay()
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        val caller = settingsRepository.getCaller()
        val owner = ServiceComposeOwner().also { it.performCreate(); it.performStart(); it.performResume() }
        composeOwner = owner

        val view = ComposeView(this).also { composeView ->
            owner.attachToView(composeView)
            composeView.setContent {
                OverlayTheme {
                    IncomingCallOverlay(
                        caller = caller,
                        onAnswer = { answerOverlay() },
                        onDecline = { declineOverlay() },
                        onCardClick = { upgradeToFullscreen() },
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP; y = (12 * resources.displayMetrics.density).toInt() }

        val wm = getSystemService(WindowManager::class.java)
        val added = runCatching { wm.addView(view, params); windowManager = wm; overlayView = view; true }.getOrDefault(false)

        if (!added) {
            disposeOwner()
            upgradeToFullscreen()
            return
        }

        if (!virtualCallRepository.tryMarkRinging()) {
            Log.w(TAG, "showOverlay: VirtualCallSession not IDLE, another call may be active")
        }
        ringtonePlayer.start(caller.customRingtoneUri, RingtonePlayer.OVERLAY_VIBRATION)
    }

    private fun answerOverlay() {
        stopAlerting()
        ServiceStarter.safeStartService(this, Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_ANSWER_CALL })
        runCatching {
            startActivity(Intent(this, VirtualCallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(VirtualCallActivity.EXTRA_START_IN_CALL, true)
            })
        }.onFailure { Log.e(TAG, "Failed to start VirtualCallActivity from answerOverlay", it) }
        stopSelf()
    }

    private fun declineOverlay() {
        stopAlerting()
        ServiceStarter.safeStartService(this, Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_DISMISS_CALL })
        stopSelf()
    }

    private fun upgradeToFullscreen() {
        stopAlerting()
        ServiceStarter.safeStartService(this, Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_SHOW_FULLSCREEN_CALL })
        stopSelf()
    }

    private fun stopAlerting() { ringtonePlayer.stop() }

    private fun removeOverlay() {
        overlayView?.let { v -> runCatching { windowManager?.removeView(v) } }
        overlayView = null
        windowManager = null
        disposeOwner()
    }

    private fun disposeOwner() { composeOwner?.performDestroy(); composeOwner = null }

    override fun onDestroy() {
        stopAlerting()
        removeOverlay()
        if (virtualCallRepository.isRinging()) {
            Log.w(TAG, "onDestroy: cleaning up stale RINGING state")
            virtualCallRepository.clear()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object { private const val TAG = "OverlayService" }
}
