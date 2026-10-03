package com.muchen.virtualcall.domain.service

interface ServiceController {

    fun ensureRunning()

    fun refreshNotification()

    fun triggerTestCall()

    fun notifyCallAnswered()

    fun notifyCallDismissed()

    fun stopService()

    fun hideOverlay()
}
