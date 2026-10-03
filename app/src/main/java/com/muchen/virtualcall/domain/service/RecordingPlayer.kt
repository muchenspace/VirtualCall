package com.muchen.virtualcall.domain.service

import android.net.Uri

interface RecordingPlayer {

    fun start(uri: Uri?)

    fun stop()

    fun setSpeakerOn(on: Boolean)

    fun setMuted(muted: Boolean)

    fun pause()

    fun resume()
}
