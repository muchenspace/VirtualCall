package com.muchen.virtualcall.domain.model

/**
 * 虚拟来电状态机状态。
 * IDLE → RINGING → INCALL → IDLE
 */
enum class CallState {
    IDLE,
    RINGING,
    INCALL,
}
