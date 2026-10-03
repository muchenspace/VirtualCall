package com.muchen.virtualcall.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

object ServiceStarter {

    private const val TAG = "ServiceStarter"

    fun safeStartForegroundService(context: Context, intent: Intent): Boolean {
        return runCatching {
            ContextCompat.startForegroundService(context, intent)
            true
        }.onFailure {
            Log.e(TAG, "Failed to start foreground service: ${intent.action}", it)
        }.getOrDefault(false)
    }

    fun safeStartService(context: Context, intent: Intent): Boolean {
        return runCatching {
            context.startService(intent)
            true
        }.onFailure {
            Log.e(TAG, "Failed to start service: ${intent.action}", it)
        }.getOrDefault(false)
    }
}
