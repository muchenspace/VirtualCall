package com.muchen.virtualcall.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.service.VirtualCallService
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServiceRestartReceiver : BroadcastReceiver() {

    @Inject
    lateinit var systemRepository: SystemRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ServiceActions.ACTION_RESTART_SERVICE) {
            return
        }
        if (!systemRepository.isServiceArmed() || systemRepository.isUserDisarmed()) {
            Log.d(TAG, "Skipping delayed restart because service is off")
            return
        }
        Log.d(TAG, "Running delayed restart recovery")
        ServiceStarter.safeStartForegroundService(
            context,
            Intent(context, VirtualCallService::class.java).apply {
                action = ServiceActions.ACTION_ENSURE_RUNNING
            }
        )
    }

    companion object {
        private const val TAG = "ServiceRestartReceiver"
    }
}
