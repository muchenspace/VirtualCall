package com.muchen.virtualcall.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.muchen.virtualcall.R
import com.muchen.virtualcall.VirtualCallActivity
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.service.ServiceActions
import com.muchen.virtualcall.service.VirtualCallService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemRepository: SystemRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val notificationMutex = Mutex()

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_service_status),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.channel_service_status_desc)
                setShowBadge(false)
            }
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_CALL,
                context.getString(R.string.channel_incoming_call),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.channel_incoming_call_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }

    fun buildServiceNotification(): Notification {
        val accessibilityEnabled = systemRepository.isAccessibilityEnabled()
        val armed = systemRepository.isServiceArmed()
        val (title, text) = when {
            !accessibilityEnabled -> context.getString(R.string.notification_accessibility_disabled) to
                context.getString(R.string.notification_accessibility_disabled_hint)
            !armed -> context.getString(R.string.notification_service_not_armed) to
                context.getString(R.string.notification_service_not_armed_hint)
            else -> context.getString(R.string.service_ready_title) to
                context.getString(R.string.service_ready_text)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    fun refreshServiceNotification() {
        scope.launch {
            notificationMutex.withLock {
                val notification = buildServiceNotification()
                runCatching {
                    context.getSystemService(NotificationManager::class.java)
                        ?.notify(NOTIFICATION_ID, notification)
                }.onFailure { Log.e(TAG, "refreshServiceNotification failed", it) }
            }
        }
    }

    fun showFullScreenCallNotification(callerName: String, forceInCall: Boolean) {
        val fullScreenIntent = Intent(context, VirtualCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra(VirtualCallActivity.EXTRA_START_IN_CALL, forceInCall)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val dismissIntent = Intent(context, VirtualCallService::class.java).apply {
            action = ServiceActions.ACTION_DISMISS_CALL
        }
        val dismissPendingIntent = PendingIntent.getService(
            context, 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CALL)
            .setContentTitle(context.getString(R.string.call_notification_title_template, callerName))
            .setContentText(context.getString(R.string.call_notification_text))
            .setSmallIcon(R.drawable.ic_phone)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .addAction(R.drawable.ic_call_end, context.getString(R.string.action_decline), dismissPendingIntent)
            .build()
        runCatching {
            context.getSystemService(NotificationManager::class.java)
                ?.notify(VIRTUAL_CALL_NOTIFICATION_ID, notification)
        }.onFailure { Log.e(TAG, "notify full-screen call failed", it) }
    }

    fun cancelCallNotification() {
        context.getSystemService(NotificationManager::class.java)?.cancel(VIRTUAL_CALL_NOTIFICATION_ID)
    }

    fun buildOverlayNotification(callerName: String): Notification {
        val dismissIntent = Intent(context, VirtualCallService::class.java).apply {
            action = ServiceActions.ACTION_DISMISS_CALL
        }
        val dismissPendingIntent = PendingIntent.getService(
            context, 2, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID_CALL)
            .setContentTitle(context.getString(R.string.call_notification_title_template, callerName))
            .setContentText(context.getString(R.string.call_notification_text))
            .setSmallIcon(R.drawable.ic_phone)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .addAction(R.drawable.ic_call_end, context.getString(R.string.action_decline), dismissPendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "CallNotificationHelper"
        const val CHANNEL_ID = "virtual_call_service_channel_v2"
        const val CHANNEL_ID_CALL = "virtual_call_call_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val VIRTUAL_CALL_NOTIFICATION_ID = 1002
        const val OVERLAY_NOTIFICATION_ID = 1003
    }
}
