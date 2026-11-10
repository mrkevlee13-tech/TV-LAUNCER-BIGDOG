package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity

/**
 * Intercepts hardware Home button presses and Xiaomi/Google stock TV launcher foreground events
 * so the TV reliably lands on BigDog Launcher (B2) both on boot and whenever Home is pressed.
 */
class BigDogHomeAccessibilityService : AccessibilityService() {

    private var lastLaunchTimestampMs: Long = 0L

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
            }
        }
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
        isServiceRunning = false
        super.onDestroy()
    }

    private fun bringLauncherToFront(reason: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastLaunchTimestampMs < DEBOUNCE_MS) return
        lastLaunchTimestampMs = now
        lastInterceptReason = reason

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(EXTRA_INTERCEPT_REASON, reason)
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
