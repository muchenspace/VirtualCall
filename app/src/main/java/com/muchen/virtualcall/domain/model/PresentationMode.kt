package com.muchen.virtualcall.domain.model

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
