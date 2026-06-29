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
 * 开机 / 包更新后自动恢复武装状态。
 * 仅当 [SystemRepository.isServiceArmed] 返回 true 时才拉起 [VirtualCallService]。
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var systemRepository: SystemRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (!systemRepository.isServiceArmed()) {
                    Log.d(TAG, "Skipping boot recovery for ${intent.action} because trigger is not armed")
                    return
                }
                Log.d(TAG, "Attempting boot recovery for ${intent.action}")
                val serviceIntent = Intent(context, VirtualCallService::class.java).apply {
                    action = ServiceActions.ACTION_ENSURE_RUNNING
                }
                ServiceStarter.safeStartForegroundService(context, serviceIntent)
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
