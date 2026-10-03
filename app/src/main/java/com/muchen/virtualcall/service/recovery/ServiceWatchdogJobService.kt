package com.muchen.virtualcall.service.recovery

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.muchen.virtualcall.domain.repository.SystemRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServiceWatchdogJobService : JobService() {

    @Inject lateinit var systemRepository: SystemRepository
    @Inject lateinit var recoveryHelper: ServiceRecoveryHelper

    override fun onStartJob(params: JobParameters?): Boolean {
        val armed = systemRepository.isServiceArmed()
        val userDisarmed = systemRepository.isUserDisarmed()
        Log.d(TAG, "Watchdog tick; armed=$armed userDisarmed=$userDisarmed")
        if (armed && !userDisarmed) {
            recoveryHelper.requestImmediateRecovery("watchdog")
        } else {
            recoveryHelper.cancelWatchdog()
        }
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean = false

    companion object {
        private const val TAG = "ServiceWatchdog"
    }
}
