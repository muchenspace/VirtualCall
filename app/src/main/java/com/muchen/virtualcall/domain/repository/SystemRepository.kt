package com.muchen.virtualcall.domain.repository

interface SystemRepository {

    fun isAccessibilityEnabled(): Boolean

    fun isBackgroundPopupGranted(): Boolean

    fun isOverlayPermissionGranted(): Boolean

    fun isServiceArmed(): Boolean

    fun setServiceArmed(armed: Boolean)

    fun isUserDisarmed(): Boolean

    fun setUserDisarmed(disarmed: Boolean)

    fun isAccessibilityGuideDismissed(): Boolean

    fun setAccessibilityGuideDismissed(dismissed: Boolean)

    fun isIgnoringBatteryOptimizations(): Boolean

    fun isFullScreenIntentGranted(): Boolean

    fun isExactAlarmAllowed(): Boolean
}
