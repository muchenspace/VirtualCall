package com.muchen.virtualcall.service.recovery

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.muchen.virtualcall.receiver.ServiceRestartReceiver
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.service.VirtualCallService
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务崩溃恢复管理：集中处理即时恢复、延迟重启调度和取消。
 * 从 [VirtualCallService] 抽取，保持恢复逻辑单一职责。
 *
 * 持有 [isStoppingExplicitly] 标志：由 Service 生命周期方法设置，
 * 决定是否在意外销毁时执行恢复。
 */
@Singleton
class ServiceRecoveryHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Volatile
    var isStoppingExplicitly = false
        private set

    /** 标记服务为显式停止状态，取消已调度的重启。 */
    fun markStopping() {
        isStoppingExplicitly = true
        cancelScheduledRestart()
    }

    /** 标记服务为运行状态。 */
    fun markRunning() {
        isStoppingExplicitly = false
    }

    /** 请求即时恢复：重新拉起前台服务。 */
    fun requestImmediateRecovery(reason: String) {
        if (isStoppingExplicitly) return
        Log.d(TAG, "Requesting immediate recovery because $reason")
        ServiceStarter.safeStartForegroundService(
            context,
            Intent(context, VirtualCallService::class.java).apply {
                action = ServiceActions.ACTION_ENSURE_RUNNING
            },
        )
    }

    /** 调度延迟重启：通过 AlarmManager 在 [delayMs] 后触发恢复。 */
    fun scheduleRestart(reason: String, delayMs: Long = RESTART_DELAY_MS) {
        if (isStoppingExplicitly) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: run { Log.e(TAG, "AlarmManager unavailable"); return }
        val triggerAtMillis = System.currentTimeMillis() + delayMs
        Log.d(TAG, "Scheduling delayed recovery in ${delayMs}ms because $reason")
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, buildRestartPendingIntent())
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, buildRestartPendingIntent())
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, buildRestartPendingIntent())
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, buildRestartPendingIntent())
            }
        }.onFailure { Log.e(TAG, "Failed to schedule restart alarm", it) }
    }

    /** 取消已调度的重启。 */
    fun cancelScheduledRestart() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        runCatching { alarmManager.cancel(buildRestartPendingIntent()) }
            .onFailure { Log.e(TAG, "Failed to cancel restart alarm", it) }
    }

    private fun buildRestartPendingIntent(): PendingIntent {
        val intent = Intent(context, ServiceRestartReceiver::class.java).apply {
            action = ServiceActions.ACTION_RESTART_SERVICE
        }
        return PendingIntent.getBroadcast(
            context, RESTART_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAG = "ServiceRecoveryHelper"
        private const val RESTART_DELAY_MS = 1_500L
        private const val RESTART_REQUEST_CODE = 4
    }
}
