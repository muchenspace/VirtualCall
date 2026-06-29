package com.muchen.virtualcall.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.muchen.virtualcall.di.AccessibilityEntryPoint
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.EntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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

        systemRepository.setServiceArmed(true)
        ServiceStarter.safeStartForegroundService(
            this,
            Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_ENSURE_RUNNING }
        )
        // 无障碍重新开启后刷新通知文案（恢复「服务就绪」或「服务未开启」）
        ServiceStarter.safeStartForegroundService(
            this,
            Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_REFRESH_NOTIFICATION }
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // 不清除 service_armed，也不停止 VirtualCallService：
        // 1. 系统关机/重启时也会触发 onUnbind，清除标志会导致重启后 BootReceiver 无法恢复服务。
        // 2. 关闭无障碍后无法触发来电（无按键事件），通知栏会显示「无障碍未开启」提示用户。
        // 3. 用户重新开启无障碍后，onServiceConnected 会恢复武装并刷新通知。
        Log.d(TAG, "onUnbind: accessibility disabled; refreshing notification to 'accessibility disabled'")
        ServiceStarter.safeStartForegroundService(
            this,
            Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_REFRESH_NOTIFICATION }
        )
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) return super.onKeyEvent(event)
        Log.d(TAG, "onKeyEvent: action=${event.action} keyCode=${event.keyCode} repeatCount=${event.repeatCount}")
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            detectDoubleClick(event.keyCode, SystemClock.elapsedRealtime())
        }
        return false
    }

    private fun detectDoubleClick(keyCode: Int, now: Long) {
        val isSameKey = keyCode == lastKeyCode
        val withinWindow = now - lastKeyEventTime < DOUBLE_CLICK_INTERVAL_MS
        Log.d(TAG, "detectDoubleClick: sameKey=$isSameKey withinWindow=$withinWindow gap=${now - lastKeyEventTime}ms")
        if (isSameKey && withinWindow) {
            if (!systemRepository.isServiceArmed()) {
                Log.d(TAG, "Detected double volume-up but service is not armed, ignoring")
                resetClickTracking()
                return
            }
            Log.d(TAG, "Detected double volume-up; starting virtual call")
            ServiceStarter.safeStartForegroundService(
                this,
                Intent(this, VirtualCallService::class.java).apply { action = ServiceActions.ACTION_TRIGGER_CALL }
            )
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
