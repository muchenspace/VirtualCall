package com.muchen.virtualcall.domain.service

import android.net.Uri

/**
 * 接通录音播放器契约：抽象通话接通后固定录音的循环播放控制。
 *
 * 抽象目的：presentation 层（Presenter）无需感知 Android [android.media.MediaPlayer]
 * 与音频路由细节，遵循 Clean Architecture 依赖规则——
 * 内层（domain）不依赖外层（基础设施）实现。
 */
interface RecordingPlayer {

    /** 开始循环播放指定录音。uri 为 null 或打开失败则不播放。 */
    fun start(uri: Uri?)

    /** 停止播放并释放资源，还原音频模式与扬声器路由。 */
    fun stop()

    /** 切换扬声器输出。 */
    fun setSpeakerOn(on: Boolean)

    /** 静音/取消静音。 */
    fun setMuted(muted: Boolean)

    /** 暂停播放（用于通话保持）。 */
    fun pause()

    /** 恢复播放（用于取消保持）。 */
    fun resume()
}
