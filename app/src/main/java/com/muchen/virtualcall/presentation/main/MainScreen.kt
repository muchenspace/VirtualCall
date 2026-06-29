package com.muchen.virtualcall.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.presentation.main.components.CustomizeCard
import com.muchen.virtualcall.presentation.main.components.HeroCard
import com.muchen.virtualcall.presentation.main.components.PowerCard
import com.muchen.virtualcall.presentation.main.components.PresentationCard
import com.muchen.virtualcall.presentation.main.components.TopAppBar
import com.muchen.virtualcall.ui.components.LocalScrolling
import com.muchen.virtualcall.ui.components.PrimaryButton
import com.muchen.virtualcall.ui.theme.LocalAppTheme

@Composable
fun MainScreen(
    state: MainUiState,
    onSaveContact: (String, String, String) -> Unit,
    onTestCall: () -> Unit,
    onToggleService: () -> Unit,
    onPreviewRingtone: () -> Unit,
    onSelectRecording: () -> Unit,
    onClearRecording: () -> Unit,
    onRestoreDefaults: () -> Unit,
    onPresentationModeChange: (PresentationMode) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenBatteryOptimization: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onOpenLockScreenGuide: () -> Unit,
) {
    val colors = LocalAppTheme.current
    val listState = rememberLazyListState()

    CompositionLocalProvider(
        LocalScrolling provides listState.isScrollInProgress,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgPrimary)
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "topbar", contentType = "header") { TopAppBar() }
            item(key = "hero", contentType = "card") {
                HeroCard(
                    armed = state.serviceArmed,
                    accessibilityEnabled = state.accessibilityEnabled,
                    onToggleService = onToggleService,
                )
            }
            item(key = "testCall", contentType = "button") {
                PrimaryButton(
                    text = stringResource(R.string.btn_test_call),
                    icon = Icons.Outlined.Call,
                    onClick = onTestCall,
                )
            }
            item(key = "presentation", contentType = "card") {
                PresentationCard(
                    presentationMode = state.presentationMode,
                    overlayPermissionGranted = state.overlayPermissionGranted,
                    presentationStatusText = state.presentationStatusText,
                    onModeChange = onPresentationModeChange,
                    onRequestOverlayPermission = onRequestOverlayPermission,
                )
            }
            item(key = "customize", contentType = "card") {
                CustomizeCard(
                    contactName = state.contactName,
                    contactNumber = state.contactNumber,
                    contactCarrier = state.contactCarrier,
                    ringtoneLabel = state.ringtoneLabel,
                    recordingLabel = state.recordingLabel,
                    hasRecording = state.hasRecording,
                    onSaveContact = onSaveContact,
                    onPreviewRingtone = onPreviewRingtone,
                    onSelectRecording = onSelectRecording,
                    onClearRecording = onClearRecording,
                    onRestoreDefaults = onRestoreDefaults,
                )
            }
            item(key = "power", contentType = "card") {
                PowerCard(
                    accessibilityEnabled = state.accessibilityEnabled,
                    batteryOptimizationIgnored = state.batteryOptimizationIgnored,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onOpenBatteryOptimization = onOpenBatteryOptimization,
                    onOpenAutoStartSettings = onOpenAutoStartSettings,
                    onOpenLockScreenGuide = onOpenLockScreenGuide,
                )
            }
            item(key = "footer", contentType = "footer") {
                Text(
                    text = stringResource(R.string.app_name) + " · v1.0",
                    color = colors.textSecondary.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
