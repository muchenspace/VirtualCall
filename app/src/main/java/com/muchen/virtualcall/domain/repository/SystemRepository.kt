package com.muchen.virtualcall.domain.repository

/**
 * 系统状态查询与跨进程标志管理 Repository 契约。
 */
interface SystemRepository {

    /** 无障碍服务是否已启用。 */
    fun isAccessibilityEnabled(): Boolean

    /** 是否拥有悬浮窗权限。 */
    fun isOverlayPermissionGranted(): Boolean

    /** 服务是否已武装（service_armed 标志）。 */
    fun isServiceArmed(): Boolean

    /**
     * 设置服务武装状态。
     * 使用同步写入（commit）确保跨进程立即可见。
     */
    fun setServiceArmed(armed: Boolean)

    /** 是否已加入电池优化白名单。 */
    fun isIgnoringBatteryOptimizations(): Boolean
}
