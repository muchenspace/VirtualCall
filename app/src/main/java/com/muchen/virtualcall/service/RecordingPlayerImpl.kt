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

class RecordingPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecordingPlayer {

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mediaPlayer: MediaPlayer? = null
    private var isSpeakerOn = false
    private var isMuted = false
    private var isPaused = false

    val isPlaying: Boolean get() = mediaPlayer != null && !isPaused

    override fun start(uri: Uri?) {
        releasePlayer()
        if (uri == null) return

        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        mp.setOnErrorListener { _, _, _ ->
            releasePlayer()
            restoreAudioMode()
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
        if (!prepared) {
            runCatching { mp.release() }
            return
        }

        runCatching { audioManager.mode = AudioManager.MODE_IN_COMMUNICATION }
        val started = runCatching { mp.start(); true }.getOrElse {
            Log.e(TAG, "Failed to start playback for recording: $uri", it)
            false
        }
        if (!started) {
            runCatching { mp.release() }
            restoreAudioMode()
            return
        }
        mediaPlayer = mp
        isPaused = false

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
        clearSpeakerRoute()
        restoreAudioMode()
    }

    private fun restoreAudioMode() {
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
