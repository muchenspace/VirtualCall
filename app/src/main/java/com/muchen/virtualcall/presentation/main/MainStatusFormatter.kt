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

/**
 * 主界面状态文本格式化器：将 domain 数据映射为展示用字符串。
 * 从 [MainPresenter] 抽取，保持 Presenter 专注于用例编排。
 */
@Singleton
class MainStatusFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** 获取铃声显示标签：自定义铃声显示标题，无则显示系统默认。 */
    fun getRingtoneLabel(customUri: Uri?): String {
        if (customUri == null) return context.getString(R.string.ringtone_system_default_name)
        val title = runCatching {
            RingtoneManager.getRingtone(context, customUri)?.getTitle(context)
        }.getOrNull()
        return title ?: context.getString(R.string.ringtone_custom_fallback_name)
    }

    /** 获取接通录音显示标签：已选择则显示文件名，否则显示未设置。 */
    fun getRecordingLabel(recordingUri: Uri?): String {
        if (recordingUri == null) return context.getString(R.string.recording_not_set)
        val fileName = queryFileName(recordingUri)
        return if (fileName != null) {
            context.getString(R.string.recording_set_prefix) + "：" + fileName
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

    /** 构建展示模式状态文本。 */
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
