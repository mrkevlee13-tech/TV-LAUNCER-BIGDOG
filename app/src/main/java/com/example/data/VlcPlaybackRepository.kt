package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.util.Log
import com.example.model.VlcPlayback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Brief, read-only media-browser connection; no notification access or playback on refresh. */
object VlcPlaybackRepository {
    const val PACKAGE = "org.videolan.vlc"

    suspend fun read(context: Context): VlcPlayback? = withController(context) { controller ->
        val metadata = controller.metadata ?: return@withController null
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
            ?: return@withController null
        val id = metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)?.takeIf { it.isNotBlank() }
            ?: return@withController null
        val state = controller.playbackState
        val mediaUri = controller.queue?.firstOrNull { it.description.mediaId == id }?.description?.mediaUri
        VlcPlayback(title, id, metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0),
            state?.position?.takeIf { it > 0 && state.state in setOf(PlaybackState.STATE_PLAYING, PlaybackState.STATE_PAUSED) },
            mediaUri?.toString(), state?.state == PlaybackState.STATE_PLAYING)
    }

    suspend fun resume(context: Context, playback: VlcPlayback): Boolean {
        val uri = playback.mediaUri ?: read(context)?.takeIf { it.mediaId == playback.mediaId }?.mediaUri ?: return false
        return runCatching {
            // VLC's exported wrapper forwards video MIME + item_location to VideoPlayerActivity.
            // Keep file URIs in VLC's own extra, not Intent.data (Android file-URI exposure).
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                component = ComponentName(PACKAGE, "$PACKAGE.StartActivity")
                type = "video/*"
                putExtra("item_location", Uri.parse(uri))
                putExtra("title", playback.title)
                playback.positionMs?.takeIf { it > 0 }?.let { putExtra("position", it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        }.onFailure { Log.w("BigDogVLC", "Unable to open VLC video", it) }.getOrDefault(false)
    }

    suspend fun pause(context: Context): Boolean = withController(context) { controller ->
        controller.transportControls.pause()
        true
    } ?: false

    private suspend fun <T> withController(context: Context, action: (MediaController) -> T): T? =
        withContext(Dispatchers.Main.immediate) {
            withTimeoutOrNull(3_500) {
                suspendCancellableCoroutine { continuation ->
                    lateinit var browser: MediaBrowser
                    fun finish(value: T?) {
                        if (!continuation.isActive) return
                        browser.disconnect()
                        continuation.resume(value)
                    }
                    browser = MediaBrowser(context.applicationContext,
                        ComponentName(PACKAGE, "$PACKAGE.PlaybackService"),
                        object : MediaBrowser.ConnectionCallback() {
                            override fun onConnected() {
                                val result = runCatching { action(MediaController(context, browser.sessionToken)) }
                                    .onFailure { Log.w("BigDogVLC", "VLC media session unavailable", it) }.getOrNull()
                                finish(result)
                            }
                            override fun onConnectionFailed() = finish(null)
                            override fun onConnectionSuspended() = finish(null)
                        }, null)
                    continuation.invokeOnCancellation { browser.disconnect() }
                    runCatching { browser.connect() }.onFailure { finish(null) }
                }
            }
        }
}
