package com.muchen.virtualcall

import android.app.Application
import android.content.Intent
import android.util.Log
import com.muchen.virtualcall.data.local.PrefsDataSource
import com.muchen.virtualcall.data.local.PrefsKeys
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.service.VirtualCallService
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VirtualCallApp : Application() {

    @Inject
    lateinit var prefsDataSource: PrefsDataSource

    override fun onCreate() {
        super.onCreate()
        val isArmed = prefsDataSource.getBoolean(PrefsKeys.KEY_SERVICE_ARMED, false)
        val userDisarmed = prefsDataSource.getBoolean(PrefsKeys.KEY_USER_DISARMED, false)
        if (!isArmed || userDisarmed) {
            Log.d(TAG, "Skipping service bootstrap because service is off")
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
