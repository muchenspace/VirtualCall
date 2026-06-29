package com.muchen.virtualcall.presentation.main

/**
 * 主界面一次性事件。
 */
sealed interface MainEvent {
    data class Toast(val resId: Int) : MainEvent
}
