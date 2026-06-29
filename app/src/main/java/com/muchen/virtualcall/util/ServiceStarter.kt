package com.muchen.virtualcall.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * 统一封装服务启动调用，捕获 ForegroundServiceStartNotAllowedException（Android 12+）、
 * IllegalStateException 等异常，避免在 BroadcastReceiver / Service / Application 等
 * 后台场景中因启动限制而崩溃。
 */
object ServiceStarter {

    private const val TAG = "ServiceStarter"

    /**
     * 安全启动前台服务。捕获所有启动异常。
     * @return true 表示启动指令已发出，false 表示启动失败（已记录日志）。
     */
    fun safeStartForegroundService(context: Context, intent: Intent): Boolean {
        return runCatching {
            ContextCompat.startForegroundService(context, intent)
            true
        }.onFailure {
            Log.e(TAG, "Failed to start foreground service: ${intent.action}", it)
        }.getOrDefault(false)
    }

    /**
     * 安全启动普通服务。捕获 IllegalStateException（Android 8+ 后台启动限制）等。
     * @return true 表示启动指令已发出，false 表示启动失败（已记录日志）。
     */
    fun safeStartService(context: Context, intent: Intent): Boolean {
        return runCatching {
            context.startService(intent)
            true
        }.onFailure {
            Log.e(TAG, "Failed to start service: ${intent.action}", it)
        }.getOrDefault(false)
    }
}
