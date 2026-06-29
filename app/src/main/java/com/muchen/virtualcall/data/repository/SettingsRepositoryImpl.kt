package com.muchen.virtualcall.data.repository

import android.content.Context
import android.net.Uri
import com.muchen.virtualcall.R
import com.muchen.virtualcall.data.local.PrefsDataSource
import com.muchen.virtualcall.data.local.PrefsKeys
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SettingsRepository 实现：通过 PrefsDataSource 读写用户配置。
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefsDataSource: PrefsDataSource,
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override fun getCaller(): Caller {
        val defaultName = context.getString(R.string.default_contact_name)
        val defaultNumber = context.getString(R.string.default_phone_number)
        val defaultCarrier = context.getString(R.string.default_carrier)
        val name = prefsDataSource.getString(PrefsKeys.KEY_CONTACT_NAME, defaultName) ?: defaultName
        val number = prefsDataSource.getString(PrefsKeys.KEY_CONTACT_NUMBER, defaultNumber) ?: defaultNumber
        val carrier = prefsDataSource.getString(PrefsKeys.KEY_CARRIER, defaultCarrier) ?: defaultCarrier
        val ringtoneUri = prefsDataSource.getString(PrefsKeys.KEY_CUSTOM_RINGTONE_URI, null)
            ?.let { runCatching { Uri.parse(it) }.getOrNull() }
        val recordingUri = prefsDataSource.getString(PrefsKeys.KEY_RECORDING_URI, null)
            ?.let { runCatching { Uri.parse(it) }.getOrNull() }
        val mode = PresentationMode.fromString(
            prefsDataSource.getString(PrefsKeys.KEY_PRESENTATION_MODE, PresentationMode.FULLSCREEN_KEY)
        )
        return Caller(name, number, ringtoneUri, mode, carrier, recordingUri)
    }

    override fun saveCaller(name: String, number: String, carrier: String) {
        val defaultName = context.getString(R.string.default_contact_name)
        val defaultNumber = context.getString(R.string.default_phone_number)
        val defaultCarrier = context.getString(R.string.default_carrier)
        prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NAME, name.trim().ifBlank { defaultName })
        prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NUMBER, number.trim().ifBlank { defaultNumber })
        prefsDataSource.putString(PrefsKeys.KEY_CARRIER, carrier.trim().ifBlank { defaultCarrier })
    }

    override fun getPresentationMode(): PresentationMode =
        PresentationMode.fromString(
            prefsDataSource.getString(PrefsKeys.KEY_PRESENTATION_MODE, PresentationMode.FULLSCREEN_KEY)
        )

    override fun savePresentationMode(mode: PresentationMode) {
        val key = when (mode) {
            PresentationMode.FULLSCREEN -> PresentationMode.FULLSCREEN_KEY
            PresentationMode.OVERLAY -> PresentationMode.OVERLAY_KEY
        }
        prefsDataSource.putString(PrefsKeys.KEY_PRESENTATION_MODE, key)
    }

    override fun getCustomRingtoneUri(): Uri? =
        prefsDataSource.getString(PrefsKeys.KEY_CUSTOM_RINGTONE_URI, null)
            ?.let { runCatching { Uri.parse(it) }.getOrNull() }

    override fun getRecordingUri(): Uri? =
        prefsDataSource.getString(PrefsKeys.KEY_RECORDING_URI, null)
            ?.let { runCatching { Uri.parse(it) }.getOrNull() }

    override fun saveRecordingUri(uri: Uri?) {
        if (uri == null) {
            prefsDataSource.remove(PrefsKeys.KEY_RECORDING_URI)
        } else {
            prefsDataSource.putString(PrefsKeys.KEY_RECORDING_URI, uri.toString())
        }
    }

    override fun restoreDefaults() {
        val defaultName = context.getString(R.string.default_contact_name)
        val defaultNumber = context.getString(R.string.default_phone_number)
        val defaultCarrier = context.getString(R.string.default_carrier)
        prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NAME, defaultName)
        prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NUMBER, defaultNumber)
        prefsDataSource.putString(PrefsKeys.KEY_CARRIER, defaultCarrier)
        prefsDataSource.putString(PrefsKeys.KEY_PRESENTATION_MODE, PresentationMode.FULLSCREEN_KEY)
        prefsDataSource.remove(PrefsKeys.KEY_CUSTOM_RINGTONE_URI)
        prefsDataSource.remove(PrefsKeys.KEY_RECORDING_URI)
    }
}
