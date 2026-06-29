package com.muchen.virtualcall.data.repository

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.muchen.virtualcall.data.local.PrefsDataSource
import com.muchen.virtualcall.data.local.PrefsKeys
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.service.VolumeKeyAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SystemRepository 实现：查询系统状态、管理跨进程 armed 标志。
 */
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

    override fun isOverlayPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    override fun isServiceArmed(): Boolean =
        prefsDataSource.getBoolean(PrefsKeys.KEY_SERVICE_ARMED, false)

    override fun setServiceArmed(armed: Boolean) {
        // 使用同步写入（commit）确保跨进程立即可见。
        // 无障碍服务运行在 :accessibility 独立进程，SharedPreferences 多进程不同步，
        // 必须使用 commit 而非 apply。
        prefsDataSource.putBooleanSync(PrefsKeys.KEY_SERVICE_ARMED, armed)
    }

    override fun isIgnoringBatteryOptimizations(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}
