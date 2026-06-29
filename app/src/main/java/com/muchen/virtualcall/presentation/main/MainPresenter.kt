package com.muchen.virtualcall.presentation.main

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
import com.muchen.virtualcall.domain.usecase.SavePresentationModeUseCase
import com.muchen.virtualcall.domain.usecase.SaveRecordingUriUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 主界面 Presenter。
 * 替代 MVVM 中的 MainViewModel，遵循 Clean Architecture 规范：
 * - 接收输入（Activity 事件），执行用例编排，更新输出（UiState / Event）
 * - 不持有 Android 框架类型（无 ViewModel），生命周期由 OwningActivity 管理
 * - 通过构造函数注入依赖（Hilt 管理实例生命周期，绑定到 OwningActivity）
 */
class MainPresenter @Inject constructor(
    private val getServiceStatusUseCase: GetServiceStatusUseCase,
    private val loadCallerInfoUseCase: LoadCallerInfoUseCase,
    private val saveCallerInfoUseCase: SaveCallerInfoUseCase,
    private val savePresentationModeUseCase: SavePresentationModeUseCase,
    private val saveRecordingUriUseCase: SaveRecordingUriUseCase,
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

    /** 加载已保存的联系人信息并刷新状态。 */
    fun initialize() {
        loadContactInfo()
        refreshStatus()
    }

    /** 加载联系人姓名、号码和运营商到 UI 状态。 */
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
                _events.trySend(MainEvent.Toast(R.string.toast_load_failed))
            }
        }
    }

    /** 刷新服务状态。Settings.Secure 查询为同步 binder IPC，需在 IO 线程执行。 */
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
                        presentationMode = status.presentationMode,
                        overlayPermissionGranted = status.isOverlayPermissionGranted,
                        batteryOptimizationIgnored = status.isIgnoringBatteryOptimizations,
                        ringtoneLabel = ringtoneLabel,
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
                _events.send(MainEvent.Toast(R.string.toast_saved))
            } catch (e: Exception) {
                Log.e(TAG, "saveContactInfo failed", e)
                _events.trySend(MainEvent.Toast(R.string.toast_save_failed))
            }
        }
    }

    fun toggleService() {
        val wasArmed = _uiState.value.serviceArmed
        if (wasArmed) {
            disarmServiceUseCase()
            serviceController.refreshNotification()
            presenterScope.launch { _events.send(MainEvent.Toast(R.string.toast_service_stopped)) }
        } else {
            if (!_uiState.value.accessibilityEnabled) {
                presenterScope.launch { _events.send(MainEvent.Toast(R.string.toast_accessibility_not_enabled)) }
                return
            }
            armServiceUseCase()
            serviceController.ensureRunning()
            presenterScope.launch { _events.send(MainEvent.Toast(R.string.toast_service_started)) }
        }
        refreshStatus()
    }

    fun triggerTestCall() {
        serviceController.triggerTestCall()
    }

    /** 保存接通录音 URI（来自 SAF 选择，已持久化权限）。 */
    fun saveRecordingUri(uri: android.net.Uri) {
        presenterScope.launch {
            saveRecordingUriUseCase(uri)
            refreshStatus()
            _events.send(MainEvent.Toast(R.string.toast_recording_selected))
        }
    }

    /** 清除接通录音。 */
    fun clearRecording() {
        presenterScope.launch {
            saveRecordingUriUseCase(null)
            refreshStatus()
            _events.send(MainEvent.Toast(R.string.toast_recording_cleared))
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
                _events.send(MainEvent.Toast(R.string.toast_defaults_restored))
            } catch (e: Exception) {
                Log.e(TAG, "restoreDefaults failed", e)
                _events.trySend(MainEvent.Toast(R.string.toast_save_failed))
            }
        }
    }

    /** 清理 Presenter 资源。在 OwningActivity.onDestroy 时调用。 */
    fun onDestroy() {
        presenterScope.launch(Dispatchers.Main) {
            _events.close()
        }
    }

    companion object {
        private const val TAG = "MainPresenter"
    }
}
