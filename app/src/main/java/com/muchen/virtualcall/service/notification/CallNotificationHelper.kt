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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 来电通知管理：集中处理通知渠道创建、服务常驻通知、全屏来电通知。
 * 从 [VirtualCallService] 抽取，保持通知构建逻辑单一职责。
 */
@Singleton
class CallNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemRepository: SystemRepository,
) {

    /** 创建通知渠道（Android 8+）。 */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "服务状态", NotificationManager.IMPORTANCE_LOW).apply {
                description = "虚拟来电后台服务状态"
                setShowBadge(false)
            }
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID_CALL, "来电通知", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "虚拟来电通知与全屏来电"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }

    /** 构建服务常驻通知。根据无障碍与武装状态动态显示文案（无操作按钮）。 */
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

    /** 刷新服务常驻通知（状态变化时调用）。 */
    fun refreshServiceNotification() {
        runCatching {
            context.getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, buildServiceNotification())
        }.onFailure { Log.e(TAG, "refreshServiceNotification failed", it) }
    }

    /**
     * 构建并显示全屏来电通知。
     * 直接启动失败时（Android 10+）作为回退方案使用。
     */
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(R.drawable.ic_call_end, context.getString(R.string.action_decline), dismissPendingIntent)
            .build()
        runCatching {
            context.getSystemService(NotificationManager::class.java)?.notify(VIRTUAL_CALL_NOTIFICATION_ID, notification)
        }.onFailure { Log.e(TAG, "notify full-screen call failed", it) }
    }

    /** 取消全屏来电通知。 */
    fun cancelCallNotification() {
        context.getSystemService(NotificationManager::class.java)?.cancel(VIRTUAL_CALL_NOTIFICATION_ID)
    }

    companion object {
        private const val TAG = "CallNotificationHelper"
        const val CHANNEL_ID = "virtual_call_service_channel"
        const val CHANNEL_ID_CALL = "virtual_call_call_channel"
        const val NOTIFICATION_ID = 1001
        const val VIRTUAL_CALL_NOTIFICATION_ID = 1002
    }
}
