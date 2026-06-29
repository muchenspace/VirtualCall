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

/**
 * 统一封装来电铃声与振动播放逻辑，修复 [VirtualCallActivity] 与 [IncomingCallOverlayService]
 * 中重复且顺序混乱的回退链路。
 *
 * 回退顺序：用户自选铃声 → 系统默认铃声 → 内置 raw 资源 → 系统闹钟铃声。
 */
class RingtonePlayer(private val context: Context) {

    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    /** 是否正在播放（铃声或 MediaPlayer 任一存活即视为播放中）。 */
    val isPlaying: Boolean get() = ringtone != null || mediaPlayer != null

    /**
     * 开始播放铃声与振动。
     *
     * @param customRingtoneUri 用户自选铃声 URI（可为 null）
     * @param vibrationPattern 振动波形；传入 null 则不振动
     */
    fun start(customRingtoneUri: Uri?, vibrationPattern: LongArray = DEFAULT_VIBRATION) {
        stop()
        playRingtone(customRingtoneUri)
        if (vibrationPattern != null) startVibration(vibrationPattern)
    }

    private fun playRingtone(customUri: Uri?) {
        val uri = customUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (tryPlayRingtone(uri, looping = true)) return

        // 回退到内置 raw 资源
        if (tryPlayBuiltIn()) return

        // 最后回退到系统闹钟铃声
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        tryPlayRingtone(alarmUri, looping = false)
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
            // 设置错误监听：播放中出错时自动释放并清理引用，避免泄漏或后续操作崩溃
            mp.setOnErrorListener { _, _, _ ->
                runCatching { mp.release() }
                mediaPlayer = null
                true // 表示错误已处理，不需再回调 OnErrorListener
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

    /** 停止铃声、振动并释放资源。 */
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
        /** 全屏来电振动波形。 */
        val FULLSCREEN_VIBRATION = longArrayOf(0, 650, 350, 850, 450)
        /** 悬浮窗来电振动波形。 */
        val OVERLAY_VIBRATION = longArrayOf(0, 500, 350, 700, 500)
        private val DEFAULT_VIBRATION = FULLSCREEN_VIBRATION
    }
}
