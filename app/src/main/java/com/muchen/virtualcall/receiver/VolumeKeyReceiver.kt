package com.muchen.virtualcall.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.muchen.virtualcall.service.VirtualCallService
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.AndroidEntryPoint

/**
 * 音量键双击广播接收器（无障碍服务检测到双击后发送）。
 * 转发 [ServiceActions.ACTION_TRIGGER_CALL] 给 [VirtualCallService]。
 */
@AndroidEntryPoint
class VolumeKeyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ServiceActions.ACTION_VOLUME_UP_DOUBLE_CLICK) {
            return
        }
        val serviceIntent = Intent(context, VirtualCallService::class.java).apply {
            action = ServiceActions.ACTION_TRIGGER_CALL
        }
        ServiceStarter.safeStartForegroundService(context, serviceIntent)
    }
}
