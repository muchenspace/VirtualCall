package com.muchen.virtualcall.presentation.call

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject

class DtmfPlayer @Inject constructor() {

    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_DTMF, 80)
    }.getOrNull()

    fun play(digit: Char) {
        val toneType = mapDigit(digit) ?: return
        toneGenerator?.let { tone ->
            runCatching { tone.startTone(toneType, 150) }
        }
    }

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
