package com.muchen.virtualcall.domain.service

/**
 * 服务控制契约：抽象前台服务的启动与状态切换。
 *
 * 抽象目的：presentation 层（Presenter）无需感知 [android.content.Service]
 * 的具体实现类与 Action 常量，遵循 Clean Architecture 依赖规则——
 * 内层（domain）不依赖外层（service 基础设施）。
 */
interface ServiceController {

    /** 确保前台服务运行并武装。 */
    fun ensureRunning()

    /** 刷新常驻通知文案（无障碍 / 武装状态变化时调用）。 */
    fun refreshNotification()

    /** 触发测试来电（从 App 内发起，挂断后回到应用）。 */
    fun triggerTestCall()

    /** 通知服务来电已被接听（更新会话状态）。 */
    fun notifyCallAnswered()

    /** 通知服务来电已被挂断/ dismiss（清理会话状态）。 */
    fun notifyCallDismissed()

    /** 关闭来电悬浮窗（切换到全屏 Activity 时调用）。 */
    fun hideOverlay()
}
