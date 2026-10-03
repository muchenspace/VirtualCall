package com.muchen.virtualcall.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.muchen.virtualcall.R

class RingtonePlayer(private val context: Context) {

    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    val isPlaying: Boolean get() = ringtone != null || mediaPlayer != null

    fun start(customRingtoneUri: Uri?, vibrationPattern: LongArray = DEFAULT_VIBRATION) {
        stop()
        playRingtone(customRingtoneUri)
        if (vibrationPattern != null) startVibration(vibrationPattern)
    }

    private fun playRingtone(customUri: Uri?) {
        val uri = customUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P && tryPlayViaMediaPlayer(uri)) return
        if (tryPlayRingtone(uri, looping = true)) return

        if (tryPlayBuiltIn()) return

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        tryPlayRingtone(alarmUri, looping = false)
    }

    private fun tryPlayViaMediaPlayer(uri: Uri): Boolean {
        return runCatching {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnErrorListener { _, _, _ ->
                    runCatching { mediaPlayer?.release() }
                    mediaPlayer = null
                    true
                }
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
            mediaPlayer = mp
            true
        }.getOrDefault(false)
    }

    private fun tryPlayRingtone(uri: Uri, looping: Boolean): Boolean {
        val r = runCatching { RingtoneManager.getRingtone(context, uri) }.getOrNull() ?: return false
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                r.isLooping = looping
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                r.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
            }
            r.play()
            ringtone = r
            true
        }.getOrDefault(false)
    }

    private fun tryPlayBuiltIn(): Boolean {
        return runCatching {
            val mp = MediaPlayer.create(context, R.raw.virtual_ringtone) ?: return false

            mp.setOnErrorListener { _, _, _ ->
                runCatching { mp.release() }
                mediaPlayer = null
                true
            }
            mp.isLooping = true
            mp.start()
            mediaPlayer = mp
            true
        }.getOrDefault(false)
    }

    private fun startVibration(pattern: LongArray) {
        val v = context.getSystemService(Vibrator::class.java) ?: return
        vibrator = v
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, 0)
            }
        }.onFailure {
            android.util.Log.e("RingtonePlayer", "Vibration failed", it)
        }
    }

    fun stop() {
        runCatching { ringtone?.stop() }
        ringtone = null
        mediaPlayer?.let { mp ->
            runCatching {
                if (mp.isPlaying) mp.stop()
                mp.release()
            }
        }
        mediaPlayer = null
        runCatching { vibrator?.cancel() }
        vibrator = null
    }

    companion object {

        val FULLSCREEN_VIBRATION = longArrayOf(0, 650, 350, 850, 450)

        val OVERLAY_VIBRATION = longArrayOf(0, 500, 350, 700, 500)
        private val DEFAULT_VIBRATION = FULLSCREEN_VIBRATION
    }
}
