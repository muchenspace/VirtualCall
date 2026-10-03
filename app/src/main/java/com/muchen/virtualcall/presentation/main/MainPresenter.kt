package com.muchen.virtualcall.presentation.main

import android.net.Uri
import android.util.Log
import com.muchen.virtualcall.R
import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.service.ServiceController
import com.muchen.virtualcall.domain.usecase.ArmServiceUseCase
import com.muchen.virtualcall.domain.usecase.DisarmServiceUseCase
import com.muchen.virtualcall.domain.usecase.GetServiceStatusUseCase
import com.muchen.virtualcall.domain.usecase.LoadCallerInfoUseCase
import com.muchen.virtualcall.domain.usecase.RestoreDefaultsUseCase
import com.muchen.virtualcall.domain.usecase.SaveCallerInfoUseCase
import com.muchen.virtualcall.domain.usecase.SaveCustomRingtoneUriUseCase
import com.muchen.virtualcall.domain.usecase.SavePresentationModeUseCase
import com.muchen.virtualcall.domain.usecase.SaveRecordingUriUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private val getServiceStatusUseCase: GetServiceStatusUseCase,
    private val loadCallerInfoUseCase: LoadCallerInfoUseCase,
    private val saveCallerInfoUseCase: SaveCallerInfoUseCase,
    private val savePresentationModeUseCase: SavePresentationModeUseCase,
    private val saveRecordingUriUseCase: SaveRecordingUriUseCase,
    private val saveCustomRingtoneUriUseCase: SaveCustomRingtoneUriUseCase,
    private val restoreDefaultsUseCase: RestoreDefaultsUseCase,
    private val armServiceUseCase: ArmServiceUseCase,
    private val disarmServiceUseCase: DisarmServiceUseCase,
    private val serviceController: ServiceController,
    private val statusFormatter: MainStatusFormatter,
) {
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = Channel<MainEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastAccessibilityEnabled: Boolean? = null
    private var isDestroyed = false

    fun initialize() {
        loadContactInfo()
        refreshStatus()
    }

    fun loadContactInfo() {
        presenterScope.launch(Dispatchers.IO) {
            try {
                val caller = loadCallerInfoUseCase()
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        contactName = caller.name,
                        contactNumber = caller.number,
                        contactCarrier = caller.carrier,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadContactInfo failed", e)
                emitToast(R.string.toast_load_failed)
            }
        }
    }

    fun refreshStatus() {
        presenterScope.launch(Dispatchers.IO) {
            try {
                val status = getServiceStatusUseCase()
                val ringtoneLabel = statusFormatter.getRingtoneLabel(status.customRingtoneUri)
                val recordingLabel = statusFormatter.getRecordingLabel(status.recordingUri)
                val presentationStatusText = statusFormatter.buildPresentationStatusText(
                    status.presentationMode,
                    status.isOverlayPermissionGranted,
                )

                val accessibilityChanged = lastAccessibilityEnabled != null &&
                    lastAccessibilityEnabled != status.isAccessibilityEnabled
                if (accessibilityChanged) {
                    serviceController.refreshNotification()
                }
                lastAccessibilityEnabled = status.isAccessibilityEnabled

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        accessibilityEnabled = status.isAccessibilityEnabled,
                        serviceArmed = status.isArmed,
                        isBackgroundPopupGranted = status.isBackgroundPopupGranted,
                        presentationMode = status.presentationMode,
                        overlayPermissionGranted = status.isOverlayPermissionGranted,
                        batteryOptimizationIgnored = status.isIgnoringBatteryOptimizations,
                        fullScreenIntentGranted = status.isFullScreenIntentGranted,
                        exactAlarmAllowed = status.isExactAlarmAllowed,
                        ringtoneLabel = ringtoneLabel,
                        hasCustomRingtone = status.customRingtoneUri != null,
                        recordingLabel = recordingLabel,
                        hasRecording = status.recordingUri != null,
                        presentationStatusText = presentationStatusText,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "refreshStatus failed", e)
            }
        }
    }

    fun saveContactInfo(name: String, number: String, carrier: String) {
        presenterScope.launch {
            try {
                saveCallerInfoUseCase(name, number, carrier)
                loadContactInfo()
                refreshStatus()
                emitToast(R.string.toast_saved)
            } catch (e: Exception) {
                Log.e(TAG, "saveContactInfo failed", e)
                emitToast(R.string.toast_save_failed)
            }
        }
    }

    fun toggleService() {
        val wasArmed = _uiState.value.serviceArmed
        if (wasArmed) {
            presenterScope.launch {
                disarmServiceUseCase()
                serviceController.stopService()
                emitToast(R.string.toast_service_stopped)
                refreshStatus()
            }
        } else {
            if (!_uiState.value.accessibilityEnabled) {
                emitToast(R.string.toast_accessibility_not_enabled)
                return
            }
            if (!_uiState.value.isBackgroundPopupGranted) {
                emitToast(R.string.toast_background_popup_not_enabled)
                return
            }
            presenterScope.launch {
                armServiceUseCase()
                serviceController.ensureRunning()
                emitToast(R.string.toast_service_started)
                refreshStatus()
            }
        }
    }

    fun triggerTestCall() {
        if (!_uiState.value.isBackgroundPopupGranted) {
            emitToast(R.string.toast_background_popup_not_enabled)
            return
        }
        serviceController.triggerTestCall()
    }

    fun ringtoneUriForPreview(): Uri? = loadCallerInfoUseCase().customRingtoneUri

    fun saveRecordingUri(uri: Uri) {
        presenterScope.launch {
            saveRecordingUriUseCase(uri)
            refreshStatus()
            emitToast(R.string.toast_recording_selected)
        }
    }

    fun clearRecording() {
        presenterScope.launch {
            saveRecordingUriUseCase(null)
            refreshStatus()
            emitToast(R.string.toast_recording_cleared)
        }
    }

    fun saveCustomRingtoneUri(uri: Uri?) {
        presenterScope.launch {
            saveCustomRingtoneUriUseCase(uri)
            refreshStatus()
            emitToast(if (uri == null) R.string.toast_ringtone_cleared else R.string.toast_ringtone_selected)
        }
    }

    fun changePresentationMode(mode: PresentationMode) {
        presenterScope.launch {
            savePresentationModeUseCase(mode)
            refreshStatus()
        }
    }

    fun restoreDefaults() {
        presenterScope.launch {
            try {
                restoreDefaultsUseCase()
                loadContactInfo()
                refreshStatus()
                emitToast(R.string.toast_defaults_restored)
            } catch (e: Exception) {
                Log.e(TAG, "restoreDefaults failed", e)
                emitToast(R.string.toast_save_failed)
            }
        }
    }

    private fun emitToast(resId: Int) {
        if (isDestroyed) return
        _events.trySend(MainEvent.Toast(resId))
    }

    fun onDestroy() {
        isDestroyed = true
        _events.close()
        presenterScope.cancel()
    }

    companion object {
        private const val TAG = "MainPresenter"
    }
}
