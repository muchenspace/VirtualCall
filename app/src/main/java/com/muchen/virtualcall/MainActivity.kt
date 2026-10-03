package com.muchen.virtualcall

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.domain.service.ServiceController
import com.muchen.virtualcall.presentation.main.MainEvent
import com.muchen.virtualcall.presentation.main.MainPresenter
import com.muchen.virtualcall.presentation.main.MainScreen
import com.muchen.virtualcall.ui.theme.VirtualCallTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.activity.ComponentActivity() {

    @Inject lateinit var systemRepository: SystemRepository
    @Inject lateinit var serviceController: ServiceController
    @Inject lateinit var presenter: MainPresenter

    private var previewRingtone: Ringtone? = null
    private var previewStopJob: Job? = null
    private var accessibilityDialogShowing = false

    private val accessibilityObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            presenter.refreshStatus()
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                toast(R.string.toast_notification_denied)
            }
        }

    private val recordingPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }.onFailure { android.util.Log.e(TAG, "takePersistableUriPermission failed", it) }
            presenter.saveRecordingUri(uri)
        }

    private val ringtonePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }.onFailure { android.util.Log.e(TAG, "takePersistableUriPermission failed", it) }
            presenter.saveCustomRingtoneUri(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            val state = presenter.uiState.collectAsState().value
            VirtualCallTheme {
                MainScreen(
                    state = state,
                    onSaveContact = { name, number, carrier -> presenter.saveContactInfo(name, number, carrier) },
                    onTestCall = { presenter.triggerTestCall() },
                    onToggleService = { presenter.toggleService() },
                    onPreviewRingtone = { previewSelectedRingtone() },
                    onSelectRingtone = { ringtonePickerLauncher.launch(arrayOf("audio/*")) },
                    onClearRingtone = { presenter.saveCustomRingtoneUri(null) },
                    onSelectRecording = { recordingPickerLauncher.launch(arrayOf("audio/*")) },
                    onClearRecording = { presenter.clearRecording() },
                    onRestoreDefaults = { presenter.restoreDefaults() },
                    onPresentationModeChange = { mode -> presenter.changePresentationMode(mode) },
                    onRequestOverlayPermission = { requestOverlayPermission() },
                    onOpenAccessibilitySettings = { launchExternalIntent(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onOpenBatteryOptimization = { requestIgnoreBatteryOptimizations() },
                    onOpenBackgroundPopupSettings = { openBackgroundPopupSettings() },
                    onOpenFullScreenIntentSettings = { openFullScreenIntentSettings() },
                    onOpenExactAlarmSettings = { openExactAlarmSettings() },
                    onOpenAutoStartSettings = { openAutoStartSettings() },
                    onOpenLockScreenGuide = { openLockScreenSettings() },
                )
            }
        }

        observeEvents()
        presenter.initialize()
        if (systemRepository.isServiceArmed() && !systemRepository.isUserDisarmed()) {
            serviceController.ensureRunning()
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                presenter.events.collect { event ->
                    when (event) {
                        is MainEvent.Toast -> toast(event.resId)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), false, accessibilityObserver
        )
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_ENABLED), false, accessibilityObserver
        )
    }

    override fun onResume() {
        super.onResume()
        presenter.refreshStatus()
        maybeShowAccessibilityGuide()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        stopRingtonePreview()
    }

    override fun onStop() {
        stopRingtonePreview()
        contentResolver.unregisterContentObserver(accessibilityObserver)
        super.onStop()
    }

    override fun onDestroy() {
        stopRingtonePreview()
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        maybeShowAccessibilityGuide()
    }

    private fun maybeShowAccessibilityGuide() {
        if (accessibilityDialogShowing) return
        if (systemRepository.isAccessibilityEnabled()) return
        if (systemRepository.isAccessibilityGuideDismissed()) return
        accessibilityDialogShowing = true
        val message = getString(R.string.dialog_accessibility_message)
        val highlight = getString(R.string.dialog_accessibility_highlight)
        val spannable = android.text.SpannableStringBuilder(message)
        val start = message.indexOf(highlight)
        if (start >= 0) {
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(0xFFFF3B30.toInt()),
                start, start + highlight.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.dialog_accessibility_title)
            .setMessage(spannable)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_accessibility_positive) { _, _ ->
                accessibilityDialogShowing = false
                launchExternalIntent(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton(R.string.dialog_accessibility_negative) { _, _ ->
                accessibilityDialogShowing = false
                systemRepository.setAccessibilityGuideDismissed(true)
            }
            .setOnCancelListener { accessibilityDialogShowing = false }
            .show()
    }

    private fun openBackgroundPopupSettings() {
        if (systemRepository.isBackgroundPopupGranted()) {
            toast(R.string.toast_background_popup_already)
            return
        }
        val miuiPermIntent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
            putExtra("extra_pkgname", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchExternalIntent(miuiPermIntent)) {
            toast(R.string.toast_background_popup_hint)
            return
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchExternalIntent(intent)) {
            toast(R.string.toast_background_popup_hint)
        }
    }

    private fun openLockScreenSettings() {
        val miuiPermIntent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
            putExtra("extra_pkgname", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchExternalIntent(miuiPermIntent)) {
            toast(R.string.toast_lock_screen_hint)
            return
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchExternalIntent(intent)) {
            toast(R.string.toast_lock_screen_hint)
        }
    }

    private fun previewSelectedRingtone() {
        stopRingtonePreview()
        val ringtoneUri = presenter.ringtoneUriForPreview()
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = runCatching { RingtoneManager.getRingtone(this, ringtoneUri) }.getOrNull()
        if (ringtone == null) {
            toast(R.string.toast_preview_unavailable)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = false
        previewRingtone = ringtone
        runCatching { ringtone.play() }
        previewStopJob = lifecycleScope.launch {
            delay(RINGTONE_PREVIEW_MS)
            stopRingtonePreview()
        }
    }

    private fun stopRingtonePreview() {
        previewStopJob?.cancel()
        previewStopJob = null
        runCatching { previewRingtone?.stop() }
        previewRingtone = null
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager ?: return
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            toast(R.string.toast_battery_already_optimized)
            return
        }
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        if (!launchExternalIntent(intent)) {
            launchExternalIntent(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) return
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply { data = Uri.parse("package:$packageName") }
        launchExternalIntent(intent)
    }

    private fun openFullScreenIntentSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            toast(R.string.toast_feature_not_needed)
            return
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager?.canUseFullScreenIntent() == true) {
            toast(R.string.toast_fullscreen_intent_already)
            return
        }
        launchExternalIntent(
            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            toast(R.string.toast_feature_not_needed)
            return
        }
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (alarmManager?.canScheduleExactAlarms() == true) {
            toast(R.string.toast_exact_alarm_already)
            return
        }
        launchExternalIntent(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun openAutoStartSettings() {
        val candidates = listOf(
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent().setComponent(ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")),
            Intent().setComponent(ComponentName("com.oplus.battery", "com.oplus.startupapp.view.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity")),
            Intent().setComponent(ComponentName("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC")),
            Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")),
        ).onEach {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.putExtra("packageName", packageName)
            it.putExtra("pkg_name", packageName)
            it.putExtra("appPackage", packageName)
            it.putExtra("extra_pkgname", packageName)
        }
        for (intent in candidates) {
            if (launchExternalIntent(intent)) return
        }
        launchExternalIntent(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        toast(R.string.toast_autostart_fallback)
    }

    private fun launchExternalIntent(intent: Intent): Boolean {
        return runCatching { startActivity(intent); true }
            .onFailure { android.util.Log.e(TAG, "Failed to launch intent: $intent", it) }
            .getOrDefault(false)
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

    companion object {
        private const val TAG = "MainActivity"
        private const val RINGTONE_PREVIEW_MS = 2500L
    }
}
