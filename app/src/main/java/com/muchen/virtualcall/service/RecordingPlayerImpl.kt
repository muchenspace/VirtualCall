package com.muchen.virtualcall.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.muchen.virtualcall.domain.service.RecordingPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * [RecordingPlayer] 的基础设施实现：封装 [MediaPlayer] 与音频路由细节。
 *
 * 将 Android 框架的音频播放、扬声器切换、通信模式等细节收口于此，
 * 使 domain/presentation 层无需 import 任何基础设施类型。
 *
 * 扬声器/听筒切换策略：
 * - 使用 USAGE_VOICE_COMMUNICATION + MODE_IN_COMMUNICATION，
 *   通过 [AudioManager.setCommunicationDevice] (API 31+) 或
 *   [AudioManager.setSpeakerphoneOn] (API < 31) 切换路由
 * - MediaPlayer 手动创建，确保 setAudioAttributes 在 prepare 前调用，
 *   否则音频流类型不生效，扬声器路由无法控制
 *
 * 无 @Singleton 作用域：随 [com.muchen.virtualcall.presentation.call.VirtualCallPresenter]
 * 生命周期创建与释放。
 */
class RecordingPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecordingPlayer {

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mediaPlayer: MediaPlayer? = null
    private var isSpeakerOn = false
    private var isMuted = false
    private var isPaused = false

    /** 是否正在播放。 */
    val isPlaying: Boolean get() = mediaPlayer != null && !isPaused

    override fun start(uri: Uri?) {
        releasePlayer()
        if (uri == null) return
        // 手动创建：setAudioAttributes 必须在 prepare 前调用才生效
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        mp.setOnErrorListener { _, _, _ ->
            runCatching { mp.release() }
            mediaPlayer = null
            true
        }
        val prepared = runCatching {
            mp.setDataSource(context, uri)
            mp.isLooping = true
            mp.prepare()
            true
        }.getOrElse {
            Log.e(TAG, "Failed to prepare MediaPlayer for recording: $uri", it)
            runCatching { mp.release() }
            return
        }
        if (!prepared) return
        // 通信模式是 speakerphoneOn / setCommunicationDevice 路由生效的前提
        runCatching { audioManager.mode = AudioManager.MODE_IN_COMMUNICATION }
        mp.start()
        mediaPlayer = mp
        isPaused = false
        // 应用当前扬声器/静音状态
        applySpeakerRoute()
        applyVolume()
    }

    override fun setSpeakerOn(on: Boolean) {
        if (isSpeakerOn == on) return
        isSpeakerOn = on
        applySpeakerRoute()
    }

    override fun setMuted(muted: Boolean) {
        if (isMuted == muted) return
        isMuted = muted
        applyVolume()
    }

    override fun pause() {
        isPaused = true
        mediaPlayer?.let { mp ->
            runCatching { if (mp.isPlaying) mp.pause() }
        }
    }

    override fun resume() {
        isPaused = false
        mediaPlayer?.let { mp ->
            runCatching { if (!mp.isPlaying) mp.start() }
        }
    }

    override fun stop() {
        releasePlayer()
        isPaused = false
        // 还原扬声器与音频模式，避免影响后续系统音频
        clearSpeakerRoute()
        runCatching { audioManager.mode = AudioManager.MODE_NORMAL }
    }

    private fun applySpeakerRoute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                if (isSpeakerOn) {
                    val speaker = audioManager.availableCommunicationDevices
                        .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                    if (speaker != null) {
                        audioManager.setCommunicationDevice(speaker)
                    } else {
                        Log.w(TAG, "No built-in speaker device found")
                    }
                } else {
                    audioManager.clearCommunicationDevice()
                }
            }.onFailure { Log.w(TAG, "setCommunicationDevice failed", it) }
        } else {
            @Suppress("DEPRECATION")
            runCatching { audioManager.isSpeakerphoneOn = isSpeakerOn }
                .onFailure { Log.w(TAG, "setSpeakerphoneOn failed", it) }
        }
    }

    private fun clearSpeakerRoute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { audioManager.clearCommunicationDevice() }
        } else {
            @Suppress("DEPRECATION")
            runCatching { audioManager.isSpeakerphoneOn = false }
        }
    }

    private fun applyVolume() {
        val v = if (isMuted) 0f else 1f
        mediaPlayer?.setVolume(v, v)
    }

    private fun releasePlayer() {
        mediaPlayer?.let { mp ->
            runCatching {
                if (mp.isPlaying) mp.stop()
                mp.release()
            }
        }
        mediaPlayer = null
    }

    companion object {
        private const val TAG = "RecordingPlayer"
    }
}
