package com.muchen.virtualcall.presentation.main

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import com.muchen.virtualcall.R
import com.muchen.virtualcall.domain.model.PresentationMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainStatusFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun getRingtoneLabel(customUri: Uri?): String {
        if (customUri == null) return context.getString(R.string.ringtone_system_default_name)
        val title = runCatching {
            RingtoneManager.getRingtone(context, customUri)?.getTitle(context)
        }.getOrNull()
        return title ?: context.getString(R.string.ringtone_custom_fallback_name)
    }

    fun getRecordingLabel(recordingUri: Uri?): String {
        if (recordingUri == null) return context.getString(R.string.recording_not_set)
        val fileName = queryFileName(recordingUri)
        return if (fileName != null) {
            context.getString(R.string.recording_set_format, fileName)
        } else {
            context.getString(R.string.recording_set_prefix)
        }
    }

    private fun queryFileName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull()
    }

    fun buildPresentationStatusText(
        mode: PresentationMode,
        overlayGranted: Boolean,
    ): String {
        return if (mode == PresentationMode.OVERLAY) {
            if (overlayGranted) context.getString(R.string.status_presentation_overlay_ready)
            else context.getString(R.string.status_presentation_overlay_missing)
        } else {
            context.getString(R.string.status_presentation_fullscreen)
        }
    }
}
