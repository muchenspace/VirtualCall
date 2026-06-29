package com.muchen.virtualcall.presentation.call

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject

/**
 * DTMF 拨号音播放器：封装 [ToneGenerator] 的构造与音调映射。
 * 从 [VirtualCallPresenter] 抽取，保持 Presenter 专注于通话状态编排。
 *
 * 无 @Singleton 作用域：随 [VirtualCallPresenter] 生命周期创建与释放，
 * 与原内联 ToneGenerator 行为一致。
 * ToneGenerator 构造可能抛出 RuntimeException，内部已做安全处理。
 */
class DtmfPlayer @Inject constructor() {

    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_DTMF, 80)
    }.getOrNull()

    /** 播放指定数字/符号对应的 DTMF 音调（150ms）。 */
    fun play(digit: Char) {
        val toneType = mapDigit(digit) ?: return
        toneGenerator?.let { tone ->
            runCatching { tone.startTone(toneType, 150) }
        }
    }

    /** 释放资源。 */
    fun release() {
        runCatching { toneGenerator?.release() }
    }

    private fun mapDigit(digit: Char): Int? = when (digit) {
        '0' -> ToneGenerator.TONE_DTMF_0
        '1' -> ToneGenerator.TONE_DTMF_1
        '2' -> ToneGenerator.TONE_DTMF_2
        '3' -> ToneGenerator.TONE_DTMF_3
        '4' -> ToneGenerator.TONE_DTMF_4
        '5' -> ToneGenerator.TONE_DTMF_5
        '6' -> ToneGenerator.TONE_DTMF_6
        '7' -> ToneGenerator.TONE_DTMF_7
        '8' -> ToneGenerator.TONE_DTMF_8
        '9' -> ToneGenerator.TONE_DTMF_9
        '*' -> ToneGenerator.TONE_DTMF_S
        '#' -> ToneGenerator.TONE_DTMF_P
        else -> null
    }
}
