package com.example.data

import android.media.tv.TvInputManager

// Use the service's explicit HDMI number, never its position in the returned list
// or an unrelated HW number (on this TV HDMI 2 is HW6).
internal fun hardwareHdmiPort(inputId: String): Int? =
    Regex("hdmi[ _-]?(\\d+)(?!\\d)", RegexOption.IGNORE_CASE)
        .find(inputId)?.groupValues?.get(1)?.toIntOrNull()

internal fun hardwareInputStatus(state: Int?): String = when (state) {
    TvInputManager.INPUT_STATE_CONNECTED -> "Device connected"
    // This ROM also returns standby for empty ports. Do not imply a cable or
    // usable picture is present; a later TvView video callback verifies video.
    TvInputManager.INPUT_STATE_CONNECTED_STANDBY -> "Standby / connection unverified"
    TvInputManager.INPUT_STATE_DISCONNECTED -> "Disconnected"
    else -> "State unavailable"
}

/** A connected ARC cable does not establish that a downstream console is sending video. */
internal fun videoSignalStatus(reason: Int): String = when (reason) {
    TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING -> "Checking video"
    TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING -> "Buffering video"
    TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY -> "Audio only"
    TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL -> "Weak video signal"
    TvInputManager.VIDEO_UNAVAILABLE_REASON_NOT_CONNECTED -> "No video signal"
    else -> "Video unavailable"
}
