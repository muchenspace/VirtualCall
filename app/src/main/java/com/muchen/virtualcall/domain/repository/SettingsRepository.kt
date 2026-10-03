package com.muchen.virtualcall.domain.repository

import android.net.Uri
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.domain.model.PresentationMode

interface SettingsRepository {

    fun getCaller(): Caller

    fun saveCaller(name: String, number: String, carrier: String)

    fun getPresentationMode(): PresentationMode

    fun savePresentationMode(mode: PresentationMode)

    fun getCustomRingtoneUri(): Uri?

    fun saveCustomRingtoneUri(uri: Uri?)

    fun getRecordingUri(): Uri?

    fun saveRecordingUri(uri: Uri?)

    fun restoreDefaults()
}
