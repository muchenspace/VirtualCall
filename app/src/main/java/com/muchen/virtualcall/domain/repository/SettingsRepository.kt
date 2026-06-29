package com.muchen.virtualcall.domain.repository

import android.net.Uri
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.domain.model.PresentationMode

/**
 * 用户配置数据 Repository 契约。
 * 管理联系人信息、铃声、展示模式等持久化配置。
 */
interface SettingsRepository {

    /** 加载联系人信息快照。 */
    fun getCaller(): Caller

    /** 保存联系人姓名、号码和运营商。 */
    fun saveCaller(name: String, number: String, carrier: String)

    /** 获取当前展示模式。 */
    fun getPresentationMode(): PresentationMode

    /** 保存展示模式。 */
    fun savePresentationMode(mode: PresentationMode)

    /** 获取自定义铃声 URI（可为 null）。 */
    fun getCustomRingtoneUri(): Uri?

    /** 获取接通后播放的固定录音 URI（可为 null）。 */
    fun getRecordingUri(): Uri?

    /** 保存接通后录音 URI。 */
    fun saveRecordingUri(uri: Uri?)

    /** 恢复所有配置为默认值。 */
    fun restoreDefaults()
}
