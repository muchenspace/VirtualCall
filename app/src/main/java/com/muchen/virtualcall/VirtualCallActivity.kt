package com.muchen.virtualcall

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import com.muchen.virtualcall.domain.service.ServiceController
import com.muchen.virtualcall.presentation.call.VirtualCallPresenter
import com.muchen.virtualcall.presentation.call.VirtualCallScreen
import com.muchen.virtualcall.ui.theme.CallScreenTheme
import com.muchen.virtualcall.util.RingtonePlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VirtualCallActivity : androidx.activity.ComponentActivity() {

    @Inject lateinit var serviceController: ServiceController
    @Inject lateinit var presenter: VirtualCallPresenter

    private val ringtonePlayer = RingtonePlayer(this)
    private var returnToApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceController.hideOverlay()
        setupLockScreenDisplay()

        returnToApp = intent?.getBooleanExtra(EXTRA_RETURN_TO_APP, false) == true
        val startInCall = shouldStartInCall(intent)
        presenter.initialize(startInCall)

        if (!startInCall) {
            ringtonePlayer.start(presenter.uiState.value.caller.customRingtoneUri, RingtonePlayer.FULLSCREEN_VIBRATION)
        }

        setContent {
            val state = presenter.uiState.collectAsState().value
            CallScreenTheme {
                VirtualCallScreen(
                    caller = state.caller,
                    isAccepted = state.isAccepted,
                    isConnecting = state.isConnecting,
                    callDurationSeconds = state.callDurationSeconds,
                    isMuted = state.isMuted,
                    isOnHold = state.isOnHold,
                    isSpeakerOn = state.isSpeakerOn,
                    isKeypadVisible = state.isKeypadVisible,
                    onAnswer = {
                        ringtonePlayer.stop()
                        presenter.answerCall()
                    },
                    onDecline = { endCall() },
                    onEndCall = { endCall() },
                    onToggleMute = { presenter.toggleMute() },
                    onToggleHold = { presenter.toggleHold() },
                    onToggleSpeaker = { presenter.toggleSpeaker() },
                    onToggleKeypad = { presenter.toggleKeypad() },
                    onKeypadDigit = { digit -> presenter.playDtmf(digit) },
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (presenter.uiState.value.isAccepted) endCall()
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        returnToApp = intent.getBooleanExtra(EXTRA_RETURN_TO_APP, false)
        if (shouldStartInCall(intent)) {
            ringtonePlayer.stop()
            presenter.initialize(startInCall = true)
        }
    }

    private fun endCall() {
        ringtonePlayer.stop()
        presenter.endCall()
        if (returnToApp) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
            finish()
        } else {
            finishAndRemoveTask()
        }
    }

    private fun shouldStartInCall(intent: Intent?): Boolean {
        return intent?.getBooleanExtra(EXTRA_START_IN_CALL, false) == true || presenter.shouldStartInCall()
    }

    private fun setupLockScreenDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        ringtonePlayer.stop()
        serviceController.notifyCallDismissed()
        presenter.onDestroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_START_IN_CALL = "extra_start_in_call"
        const val EXTRA_RETURN_TO_APP = "extra_return_to_app"
    }
}
