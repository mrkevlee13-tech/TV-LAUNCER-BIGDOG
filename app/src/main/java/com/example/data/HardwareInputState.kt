package com.example.data

import android.media.tv.TvInputManager

// Use the service's explicit HDMI number, never its position in the returned list
// or an unrelated HW number (on this TV HDMI 2 is HW6).
internal fun hardwareHdmiPort(inputId: String): Int? =
    Regex("hdmi[ _-]?(\\d+)(?!\\d)", RegexOption.IGNORE_CASE)
        .find(inputId)?.groupValues?.get(1)?.toIntOrNull()

internal fun hardwareInputStatus(state: Int?): String = when (state) {
    TvInputManager.INPUT_STATE_CONNECTED -> "Connected"
    // This ROM also returns standby for empty ports. Do not imply a cable or
    // usable picture is present; a later TvView video callback verifies video.
    TvInputManager.INPUT_STATE_CONNECTED_STANDBY -> "No signal / standby"
    TvInputManager.INPUT_STATE_DISCONNECTED -> "Disconnected"
    else -> "State unavailable"
}
