package com.muchen.virtualcall.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.muchen.virtualcall.domain.util.formatPhoneNumber

@Immutable
data class Caller(
    val name: String,
    val number: String,
    val customRingtoneUri: Uri?,
    val presentationMode: PresentationMode,
    val carrier: String = "中国移动",
    val recordingUri: Uri? = null,
) {
    val formattedNumber: String get() = formatPhoneNumber(number)
}
