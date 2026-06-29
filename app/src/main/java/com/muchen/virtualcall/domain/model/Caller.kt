package com.muchen.virtualcall.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.muchen.virtualcall.domain.util.formatPhoneNumber

/**
 * 来电展示所需的联系人信息快照（Domain 实体）。
 * 从 SharedPreferences 一次性读取，供 VirtualCallActivity 与 IncomingCallOverlayService 共用。
 */
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
