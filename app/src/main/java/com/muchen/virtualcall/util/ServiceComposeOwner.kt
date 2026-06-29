package com.muchen.virtualcall.util

import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * 为 [android.app.Service] 中使用的 [androidx.compose.ui.platform.ComposeView] 提供所需的生命周期、
 * SavedState、返回键分发所有者。
 *
 * ComposeView 默认从父 View 树查找这些所有者，而 Service 没有内置实现，因此需要手动挂载，
 * 否则 ComposeView 抛出 IllegalStateException。
 *
 * 注意：未挂载 ViewModelStoreOwner——悬浮窗 Composable 不使用 viewModel()，无需该所有者；
 * 若将来需要，再引入 lifecycle-viewmodel 的 setViewTreeViewModelStoreOwner。
 */
class ServiceComposeOwner : SavedStateRegistryOwner, OnBackPressedDispatcherOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val dispatcher = OnBackPressedDispatcher()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    override val onBackPressedDispatcher: OnBackPressedDispatcher get() = dispatcher

    fun performCreate(savedState: android.os.Bundle? = null) {
        runCatching {
            savedStateRegistryController.performRestore(savedState)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
    }

    fun performStart() {
        runCatching { lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START) }
    }

    fun performResume() {
        runCatching { lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
    }

    fun performDestroy() {
        runCatching { lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY) }
    }

    /** 将所有者挂载到目标 View 树，供 ComposeView 查找。 */
    fun attachToView(view: View) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewTreeOnBackPressedDispatcherOwner(this)
    }
}
