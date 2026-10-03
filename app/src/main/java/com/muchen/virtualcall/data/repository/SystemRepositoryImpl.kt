package com.muchen.virtualcall.data.repository

import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import com.muchen.virtualcall.data.local.PrefsDataSource
import com.muchen.virtualcall.data.local.PrefsKeys
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.service.VolumeKeyAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepositoryImpl @Inject constructor(
    private val prefsDataSource: PrefsDataSource,
    @ApplicationContext private val context: Context,
) : SystemRepository {

    override fun isAccessibilityEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        ) == 1
        if (!accessibilityEnabled) return false
        val expectedComponent = ComponentName(context, VolumeKeyAccessibilityService::class.java)
        val expectedShortName = expectedComponent.flattenToShortString()
        val expectedFullName = expectedComponent.flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':').any { flattened ->
            val component = ComponentName.unflattenFromString(flattened)
            when {
                component != null -> component.packageName == expectedComponent.packageName &&
                    component.className == expectedComponent.className
                else -> flattened.equals(expectedShortName, ignoreCase = true) ||
                    flattened.equals(expectedFullName, ignoreCase = true)
            }
        }
    }

    override fun isBackgroundPopupGranted(): Boolean {
        if (!isMiui()) return true
        return runCatching {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return true
            val method = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java,
            )
            val mode = method.invoke(appOps, OP_BACKGROUND_START_ACTIVITY, Process.myUid(), context.packageName) as Int
            mode == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(true)
    }

    private fun isMiui(): Boolean =
        runCatching { Class.forName("miui.os.Build") }.isSuccess

    override fun isOverlayPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    override fun isServiceArmed(): Boolean =
        prefsDataSource.getBoolean(PrefsKeys.KEY_SERVICE_ARMED, false)

    override fun setServiceArmed(armed: Boolean) {
        prefsDataSource.putBoolean(PrefsKeys.KEY_SERVICE_ARMED, armed)
    }

    override fun isUserDisarmed(): Boolean =
        prefsDataSource.getBoolean(PrefsKeys.KEY_USER_DISARMED, false)

    override fun setUserDisarmed(disarmed: Boolean) {
        prefsDataSource.putBoolean(PrefsKeys.KEY_USER_DISARMED, disarmed)
    }

    override fun isAccessibilityGuideDismissed(): Boolean =
        prefsDataSource.getBoolean(PrefsKeys.KEY_ACCESSIBILITY_GUIDE_DISMISSED, false)

    override fun setAccessibilityGuideDismissed(dismissed: Boolean) {
        prefsDataSource.putBoolean(PrefsKeys.KEY_ACCESSIBILITY_GUIDE_DISMISSED, dismissed)
    }

    override fun isIgnoringBatteryOptimizations(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun isFullScreenIntentGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return false
        return notificationManager.canUseFullScreenIntent()
    }

    override fun isExactAlarmAllowed(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        return alarmManager.canScheduleExactAlarms()
    }

    companion object {
        private const val OP_BACKGROUND_START_ACTIVITY = 10021
    }
}
