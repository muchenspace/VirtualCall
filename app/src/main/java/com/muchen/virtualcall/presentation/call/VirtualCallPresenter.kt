package com.muchen.virtualcall.presentation.call

import android.util.Log
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.service.RecordingPlayer
import com.muchen.virtualcall.domain.service.ServiceController
import com.muchen.virtualcall.domain.usecase.AnswerCallUseCase
import com.muchen.virtualcall.domain.usecase.EndCallUseCase
import com.muchen.virtualcall.domain.usecase.LoadCallerInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 来电/通话界面 Presenter。
 * 替代 MVVM 中的 VirtualCallViewModel，遵循 Clean Architecture 规范：
 * - 接收输入（Activity 事件），执行用例编排，更新输出（UiState）
 * - 不持有 Android 框架类型（无 ViewModel），生命周期由 OwningActivity 管理
 * - 通过构造函数注入依赖（Hilt 管理实例生命周期，绑定到 OwningActivity）
 */
class VirtualCallPresenter @Inject constructor(
    private val loadCallerInfoUseCase: LoadCallerInfoUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
    private val endCallUseCase: EndCallUseCase,
    private val virtualCallRepository: VirtualCallRepository,
    private val serviceController: ServiceController,
    private val dtmfPlayer: DtmfPlayer,
    private val recordingPlayer: RecordingPlayer,
) {
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(VirtualCallUiState())
    val uiState: StateFlow<VirtualCallUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var connectingJob: Job? = null
    private var isCallHandled = false

    /** 初始化来电状态。 */
    fun initialize(startInCall: Boolean) {
        val caller = loadCallerInfoUseCase()
        if (startInCall) {
            answerCallUseCase()
            showInCallState(connected = true)
            _uiState.value = _uiState.value.copy(caller = caller, isAccepted = true)
            recordingPlayer.start(caller.recordingUri)
        } else {
            if (!virtualCallRepository.tryMarkRinging()) {
                Log.w(TAG, "initialize: VirtualCallSession not IDLE, another call may be active")
            }
            _uiState.value = _uiState.value.copy(caller = caller, isAccepted = false)
        }
    }

    /** 接听来电。 */
    fun answerCall() {
        if (isCallHandled) return
        isCallHandled = true
        serviceController.notifyCallAnswered()
        showInCallState(connected = false)
        connectingJob?.cancel()
        connectingJob = presenterScope.launch {
            delay(CALL_CONNECT_DELAY_MS)
            answerCallUseCase()
            _uiState.value = _uiState.value.copy(isConnecting = false)
            isCallHandled = false
            startCallTimer()
            recordingPlayer.start(_uiState.value.caller.recordingUri)
        }
    }

    /** 结束通话。终止操作不受 isCallHandled 拦截。 */
    fun endCall() {
        connectingJob?.cancel()
        timerJob?.cancel()
        recordingPlayer.stop()
        endCallUseCase()
        serviceController.notifyCallDismissed()
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
        recordingPlayer.setMuted(newMuted)
    }

    fun toggleHold() {
        val newHold = !_uiState.value.isOnHold
        _uiState.value = _uiState.value.copy(isOnHold = newHold)
        if (newHold) recordingPlayer.pause() else recordingPlayer.resume()
    }

    fun toggleSpeaker() {
        val newSpeaker = !_uiState.value.isSpeakerOn
        _uiState.value = _uiState.value.copy(isSpeakerOn = newSpeaker)
        recordingPlayer.setSpeakerOn(newSpeaker)
    }

    fun toggleKeypad() {
        _uiState.value = _uiState.value.copy(isKeypadVisible = !_uiState.value.isKeypadVisible)
    }

    fun playDtmf(digit: Char) {
        dtmfPlayer.play(digit)
    }

    fun shouldStartInCall(): Boolean = virtualCallRepository.isInCall()

    // --- 内部方法 ---

    private fun showInCallState(connected: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAccepted = true,
            isConnecting = !connected,
        )
        if (connected) {
            isCallHandled = false
            startCallTimer()
        }
    }

    private fun startCallTimer() {
        _uiState.value = _uiState.value.copy(callDurationSeconds = 0)
        timerJob?.cancel()
        timerJob = presenterScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isOnHold) {
                    _uiState.value = _uiState.value.copy(
                        callDurationSeconds = _uiState.value.callDurationSeconds + 1
                    )
                }
            }
        }
    }

    /** 清理 Presenter 资源。在 OwningActivity.onDestroy 时调用。 */
    fun onDestroy() {
        connectingJob?.cancel()
        timerJob?.cancel()
        recordingPlayer.stop()
        dtmfPlayer.release()
        presenterScope.cancel()
    }

    companion object {
        private const val TAG = "VirtualCallPresenter"
        private const val CALL_CONNECT_DELAY_MS = 1500L
    }
}
