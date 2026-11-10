package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.ComponentName
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity
import com.example.data.VlcPlaybackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Intercepts hardware Home button presses and Xiaomi/Google stock TV launcher foreground events
 * so the TV reliably lands on BigDog Launcher (B2) both on boot and whenever Home is pressed.
 */
class BigDogHomeAccessibilityService : AccessibilityService() {

    private var lastLaunchTimestampMs: Long = 0L
    private var lastWindowPackage: String? = null
    private var foregroundAppPackage: String? = null
    private var foregroundAppTitle: String? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.i(TAG, "BigDogHomeAccessibilityService connected and active.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            val isFallbackHome = pkg == "com.android.tv.settings" &&
                event.className?.toString() == "com.android.tv.settings.system.FallbackHome"
            if (pkg in STOCK_LAUNCHER_PACKAGES || isFallbackHome) {
                Log.i(TAG, "Detected stock launcher window ($pkg), redirecting to BigDog Launcher.")
                bringLauncherToFront(reason = "stock_window:$pkg")
            } else {
                trackOpenedApp(pkg, event.className?.toString())
            }
        }
    }

    private fun trackOpenedApp(pkg: String, className: String?) {
        if (pkg == packageName) {
            lastWindowPackage = pkg
            foregroundAppPackage = null
            foregroundAppTitle = null
            return
        }
        if (pkg == lastWindowPackage || className == null ||
            pkg.startsWith("com.android.") || pkg == "android" ||
            pkg in setOf("com.xiaomi.mitv.settings", "com.xiaomi.mitv.systemui",
                "com.xiaomi.tvqs", "com.xiaomi.mitv.smartpanel")) return
        val pm = packageManager
        // Ignore keyboards, system dialogs and file-picker overlays, but recognise player activities.
        val activity = runCatching { pm.getActivityInfo(ComponentName(pkg, className), 0) }.getOrNull() ?: return
        if (className.endsWith(".PickerActivity")) return
        val launch = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg) ?: return
        val title = runCatching { pm.getApplicationLabel(activity.applicationInfo).toString() }.getOrDefault(pkg)
        lastWindowPackage = pkg
        foregroundAppPackage = launch.component?.packageName ?: pkg
        foregroundAppTitle = title
        sendBroadcast(Intent(ACTION_FOREGROUND_APP).setPackage(packageName).apply {
            putExtra(EXTRA_APP_PACKAGE, foregroundAppPackage)
            putExtra(EXTRA_APP_TITLE, title)
        })
        Log.i(TAG, "Foreground app: $pkg")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (event.keyCode == KeyEvent.KEYCODE_HOME) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                Log.i(TAG, "Intercepted KEYCODE_HOME DOWN -> launching BigDog Launcher.")
                bringLauncherToFront(reason = "KEYCODE_HOME")
            }
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {
        Log.w(TAG, "BigDogHomeAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        isServiceRunning = false
        super.onDestroy()
    }

    private fun bringLauncherToFront(reason: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastLaunchTimestampMs < DEBOUNCE_MS) return
        lastLaunchTimestampMs = now
        lastInterceptReason = reason
        if (foregroundAppPackage == VlcPlaybackRepository.PACKAGE) {
            serviceScope.launch { VlcPlaybackRepository.pause(this@BigDogHomeAccessibilityService) }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(EXTRA_INTERCEPT_REASON, reason)
            putExtra(EXTRA_APP_PACKAGE, foregroundAppPackage)
            putExtra(EXTRA_APP_TITLE, foregroundAppTitle)
        }
        runCatching {
            startActivity(intent)
        }.onFailure { e ->
            Log.e(TAG, "Failed to bring BigDog Launcher to front", e)
        }
    }

    companion object {
        private const val TAG = "BigDogHomeService"
        private const val DEBOUNCE_MS = 250L
        const val EXTRA_INTERCEPT_REASON = "extra_intercept_reason"
        const val ACTION_FOREGROUND_APP = "com.aistudio.bigdoglauncher.b2tv.FOREGROUND_APP"
        const val EXTRA_APP_PACKAGE = "foreground_app_package"
        const val EXTRA_APP_TITLE = "foreground_app_title"

        @Volatile
        var isServiceRunning: Boolean = false
            private set

        @Volatile
        var lastInterceptReason: String = "None"
            private set

        // Known Xiaomi MiTV / PatchWall and Google TV stock launcher packages
        val STOCK_LAUNCHER_PACKAGES = setOf(
            "com.google.android.tvlauncher",
            "com.google.android.apps.tv.launcherx",
            "com.xiaomi.mitv.tvplayer",
            "com.mitv.tvhome",
            "com.xiaomi.tv.home"
        )
    }
}
