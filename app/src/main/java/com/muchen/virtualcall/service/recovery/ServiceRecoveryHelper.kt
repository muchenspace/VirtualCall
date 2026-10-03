package com.muchen.virtualcall.service.recovery

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.receiver.ServiceRestartReceiver
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.service.VirtualCallService
import com.muchen.virtualcall.util.ServiceStarter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRecoveryHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemRepository: SystemRepository,
) {

    fun shouldRecover(): Boolean =
        !systemRepository.isUserDisarmed() && systemRepository.isServiceArmed()

    fun requestImmediateRecovery(reason: String) {
        if (!shouldRecover()) return
        Log.d(TAG, "Requesting immediate recovery because $reason")
        ServiceStarter.safeStartForegroundService(
            context,
            Intent(context, VirtualCallService::class.java).apply {
                action = ServiceActions.ACTION_ENSURE_RUNNING
            },
        )
    }

    fun scheduleRestart(reason: String, delayMs: Long = RESTART_DELAY_MS) {
        if (!shouldRecover()) return
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

    fun cancelScheduledRestart() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        runCatching { alarmManager.cancel(buildRestartPendingIntent()) }
            .onFailure { Log.e(TAG, "Failed to cancel restart alarm", it) }
    }

    fun scheduleWatchdog() {
        if (!shouldRecover()) {
            cancelWatchdog()
            return
        }
        val jobScheduler = context.getSystemService(JobScheduler::class.java) ?: return
        val component = ComponentName(context, ServiceWatchdogJobService::class.java)
        if (jobScheduler.getPendingJob(WATCHDOG_JOB_ID) != null) return
        val jobInfo = JobInfo.Builder(WATCHDOG_JOB_ID, component)
            .setPeriodic(WATCHDOG_INTERVAL_MS)
            .setPersisted(true)
            .build()
        runCatching { jobScheduler.schedule(jobInfo) }
            .onFailure { Log.e(TAG, "Failed to schedule watchdog job", it) }
    }

    fun cancelWatchdog() {
        val jobScheduler = context.getSystemService(JobScheduler::class.java) ?: return
        runCatching { jobScheduler.cancel(WATCHDOG_JOB_ID) }
            .onFailure { Log.e(TAG, "Failed to cancel watchdog job", it) }
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
        private const val WATCHDOG_JOB_ID = 5
        private const val WATCHDOG_INTERVAL_MS = 15 * 60 * 1000L
    }
}
