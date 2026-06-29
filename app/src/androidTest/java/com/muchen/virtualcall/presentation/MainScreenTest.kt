package com.muchen.virtualcall.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.muchen.virtualcall.MainActivity
import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.presentation.main.MainScreen
import com.muchen.virtualcall.presentation.main.MainUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MainScreen Compose UI 测试骨架。
 *
 * 使用 createAndroidComposeRule<MainActivity> 提供 Hilt 注入的 Activity 上下文。
 * 验证关键 UI 元素存在性。
 *
 * 运行前确保设备已连接：adb devices
 * 运行：./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainScreen_displaysContactName() {
        composeRule.setContent {
            MainScreen(
                state = MainUiState(
                    contactName = "测试联系人",
                    contactNumber = "13800000000",
                    serviceArmed = false,
                    presentationMode = PresentationMode.FULLSCREEN,
                ),
                onSaveContact = { _, _, _ -> },
                onTestCall = {},
                onToggleService = {},
                onPreviewRingtone = {},
                onRestoreDefaults = {},
                onPresentationModeChange = {},
                onRequestOverlayPermission = {},
                onOpenAccessibilitySettings = {},
                onOpenBatteryOptimization = {},
                onOpenAutoStartSettings = {},
                onOpenLockScreenGuide = {},
            )
        }

        composeRule.onNodeWithText("测试联系人").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysServiceToggle_whenNotArmed() {
        composeRule.setContent {
            MainScreen(
                state = MainUiState(
                    contactName = "测试",
                    contactNumber = "13800000000",
                    serviceArmed = false,
                    presentationMode = PresentationMode.FULLSCREEN,
                ),
                onSaveContact = { _, _, _ -> },
                onTestCall = {},
                onToggleService = {},
                onPreviewRingtone = {},
                onRestoreDefaults = {},
                onPresentationModeChange = {},
                onRequestOverlayPermission = {},
                onOpenAccessibilitySettings = {},
                onOpenBatteryOptimization = {},
                onOpenAutoStartSettings = {},
                onOpenLockScreenGuide = {},
            )
        }

        // 验证启动服务按钮存在（未武装状态）
        composeRule.onNodeWithText("启动服务").assertIsDisplayed()
    }
}
