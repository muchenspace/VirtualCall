package com.muchen.virtualcall

import android.app.Application
import android.content.Intent
import android.util.Log
import com.muchen.virtualcall.data.local.PrefsDataSource
import com.muchen.virtualcall.data.local.PrefsKeys
import com.muchen.virtualcall.service.VirtualCallService
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application 入口。
 * 启动时检查 service_armed 标志，若已武装则恢复前台服务。
 */
@HiltAndroidApp
class VirtualCallApp : Application() {

    @Inject
    lateinit var prefsDataSource: PrefsDataSource

    override fun onCreate() {
        super.onCreate()
        val isArmed = prefsDataSource.getBoolean(PrefsKeys.KEY_SERVICE_ARMED, false)
        if (!isArmed) {
            Log.d(TAG, "Skipping service bootstrap because trigger is not armed")
            return
        }
        Log.d(TAG, "Bootstrapping virtual call service from application startup")
        ServiceStarter.safeStartForegroundService(
            this,
            Intent(this, VirtualCallService::class.java).apply {
                action = ServiceActions.ACTION_ENSURE_RUNNING
            }
        )
    }

    companion object {
        private const val TAG = "VirtualCallApp"
    }
}
