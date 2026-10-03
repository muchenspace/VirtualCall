package com.muchen.virtualcall.presentation.main

sealed interface MainEvent {
    data class Toast(val resId: Int) : MainEvent
}
