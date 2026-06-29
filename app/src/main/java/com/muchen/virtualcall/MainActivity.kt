package com.muchen.virtualcall

import android.Manifest
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.activity.ComponentActivity() {

    @Inject lateinit var systemRepository: SystemRepository
    @Inject lateinit var serviceController: ServiceController
    @Inject lateinit var presenter: MainPresenter

    private var previewRingtone: Ringtone? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stopPreviewRunnable = Runnable { stopRingtonePreview() }
    private var accessibilityDialogShowing = false

    private val accessibilityObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            presenter.refreshStatus()
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                toast("通知权限被拒绝，来电通知功能可能受限")
            }
        }

    /** 接通录音选择器：使用 SAF OpenDocument 选取音频文件，并申请持久化 URI 权限。 */
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
                    onSelectRecording = { recordingPickerLauncher.launch(arrayOf("audio/*")) },
                    onClearRecording = { presenter.clearRecording() },
                    onRestoreDefaults = { presenter.restoreDefaults() },
                    onPresentationModeChange = { mode -> presenter.changePresentationMode(mode) },
                    onRequestOverlayPermission = { requestOverlayPermission() },
                    onOpenAccessibilitySettings = { launchExternalIntent(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onOpenBatteryOptimization = { requestIgnoreBatteryOptimizations() },
                    onOpenAutoStartSettings = { openAutoStartSettings() },
                    onOpenLockScreenGuide = { openLockScreenSettings() },
                )
            }
        }

        observeEvents()
        presenter.initialize()

        // 启动前台服务以显示状态通知
        serviceController.ensureRunning()
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
        presenter.onDestroy()
        super.onDestroy()
    }

    // --- 权限与引导 ---

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
            }
            .setOnCancelListener { accessibilityDialogShowing = false }
            .show()
    }

    private fun openLockScreenSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchExternalIntent(intent)) {
            toast(R.string.toast_lock_screen_hint)
        }
    }

    // --- 铃声预览 ---

    private fun previewSelectedRingtone() {
        stopRingtonePreview()
        val ringtoneUri = presenter.uiState.value.let { state ->
            null // 简化：从系统默认铃声预览
        } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = runCatching { RingtoneManager.getRingtone(this, ringtoneUri) }.getOrNull()
        if (ringtone == null) { toast(R.string.toast_preview_unavailable); return }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = false
        previewRingtone = ringtone
        previewRingtone?.play()
        lifecycleScope.launch {
            kotlinx.coroutines.delay(RINGTONE_PREVIEW_MS)
            stopRingtonePreview()
        }
    }

    private fun stopRingtonePreview() {
        mainHandler.removeCallbacks(stopPreviewRunnable)
        runCatching { previewRingtone?.stop() }
        previewRingtone = null
    }

    // --- 系统设置跳转 ---

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager ?: return
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            toast(R.string.toast_battery_already_optimized); return
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

    private fun openAutoStartSettings() {
        val candidates = listOf(
            Intent().setComponent(ComponentName("com.oplus.battery", "com.oplus.startupapp.view.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity")),
        ).onEach { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); it.putExtra("packageName", packageName); it.putExtra("pkg_name", packageName); it.putExtra("appPackage", packageName) }
        for (intent in candidates) { if (launchExternalIntent(intent)) return }
        launchExternalIntent(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.parse("package:$packageName"); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        toast(R.string.toast_autostart_fallback)
    }

    // --- 辅助 ---

    private fun launchExternalIntent(intent: Intent): Boolean {
        return runCatching { startActivity(intent); true }
            .onFailure { android.util.Log.e(TAG, "Failed to launch intent: $intent", it) }
            .getOrDefault(false)
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    companion object {
        private const val TAG = "MainActivity"
        private const val RINGTONE_PREVIEW_MS = 2500L
    }
}
