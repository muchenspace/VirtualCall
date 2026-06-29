package com.muchen.virtualcall.domain.model

/**
 * 来电展示模式。
 * FULLSCREEN — 全屏来电界面（VirtualCallActivity）
 * OVERLAY — 顶部悬浮窗卡片（IncomingCallOverlayService）
 */
enum class PresentationMode {
    FULLSCREEN,
    OVERLAY;

    companion object {
        fun fromString(value: String?): PresentationMode =
            if (value == OVERLAY_KEY) OVERLAY else FULLSCREEN

        const val OVERLAY_KEY = "overlay"
        const val FULLSCREEN_KEY = "fullscreen"
    }
}
