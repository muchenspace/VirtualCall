package com.muchen.virtualcall.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.muchen.virtualcall.VirtualCallActivity
import com.muchen.virtualcall.di.AccessibilityEntryPoint
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.EntryPointAccessors

class VolumeKeyAccessibilityService : AccessibilityService() {

    private var lastKeyEventTime = 0L
    private var lastKeyCode = -1

    private val systemRepository: SystemRepository by lazy {
        EntryPointAccessors.fromApplication(applicationContext, AccessibilityEntryPoint::class.java).systemRepository()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        Log.d(TAG, "onServiceConnected: flags=${serviceInfo?.flags}")

        if (systemRepository.isUserDisarmed()) {
            Log.d(TAG, "Accessibility connected but user previously disarmed; keeping service off")
            ServiceStarter.safeStartForegroundService(
                this,
                Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_REFRESH_NOTIFICATION }
            )
            return
        }
        systemRepository.setServiceArmed(true)
        ServiceStarter.safeStartForegroundService(
            this,
            Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_ENSURE_RUNNING }
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: accessibility disabled; refreshing notification")
        ServiceStarter.safeStartForegroundService(
            this,
            Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_REFRESH_NOTIFICATION }
        )
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) return super.onKeyEvent(event)
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            detectDoubleClick(event.keyCode, SystemClock.elapsedRealtime())
        }
        return false
    }

    private fun detectDoubleClick(keyCode: Int, now: Long) {
        val isSameKey = keyCode == lastKeyCode
        val withinWindow = now - lastKeyEventTime < DOUBLE_CLICK_INTERVAL_MS
        if (isSameKey && withinWindow) {
            if (!systemRepository.isServiceArmed() || systemRepository.isUserDisarmed()) {
                Log.d(TAG, "Detected double volume-up but service is not armed, ignoring")
                resetClickTracking()
                return
            }
            Log.d(TAG, "Detected double volume-up; starting virtual call")
            val started = ServiceStarter.safeStartForegroundService(
                this,
                Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_TRIGGER_CALL }
            )
            if (!started) {
                Log.w(TAG, "safeStartForegroundService failed, falling back to direct launch")
                val callIntent = Intent(this, VirtualCallActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
                runCatching { startActivity(callIntent) }
            }
            resetClickTracking()
        } else {
            lastKeyCode = keyCode
            lastKeyEventTime = now
        }
    }

    private fun resetClickTracking() { lastKeyCode = -1; lastKeyEventTime = 0L }

    override fun onInterrupt() = Unit

    companion object {
        private const val TAG = "VirtualCallAccessibility"
        private const val DOUBLE_CLICK_INTERVAL_MS = 700L
    }
}
