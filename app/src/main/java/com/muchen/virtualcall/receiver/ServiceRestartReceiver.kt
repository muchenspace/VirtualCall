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

/**
 * AlarmManager 延迟重启接收器：崩溃恢复链的一环。
 * 仅当 [SystemRepository.isServiceArmed] 返回 true 时才执行重启。
 */
@AndroidEntryPoint
class ServiceRestartReceiver : BroadcastReceiver() {

    @Inject
    lateinit var systemRepository: SystemRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ServiceActions.ACTION_RESTART_SERVICE) {
            return
        }
        if (!systemRepository.isServiceArmed()) {
            Log.d(TAG, "Skipping delayed restart because trigger is not armed")
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
