package com.example.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R

/** Silent visual only: never owns audio focus, and releases its decoder when hidden. */
@Composable
internal fun BigDogMotionWallpaper(modifier: Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = remember(context) { MotionWallpaperView(context) }
    AndroidView(factory = { view }, modifier = modifier)
    DisposableEffect(view) { onDispose { view.close() } }
}

/** Serial decoder ownership; vendor teardown must never block the remote/UI thread. */
private object WallpaperDecoder {
    val handler = Handler(HandlerThread("BigDogWallpaperDecoder").apply { start() }.looper)
}

private class MotionWallpaperView(context: Context) : TextureView(context), TextureView.SurfaceTextureListener {
    private var session: WallpaperSession? = null
    private var videoWidth = 1920
    private var videoHeight = 1080

    init {
        isOpaque = false
        isFocusable = false
        isClickable = false
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        close()
        session = WallpaperSession(context.applicationContext, texture) { videoW, videoH ->
            post {
                videoWidth = videoW
                videoHeight = videoH
                crop(this.width, this.height)
            }
        }
    }

    private fun crop(width: Int, height: Int) {
        if (width <= 0 || height <= 0 || videoHeight <= 0) return
        val videoAspect = videoWidth.toFloat() / videoHeight
        val viewAspect = width.toFloat() / height
        setTransform(Matrix().apply {
            setScale(maxOf(1f, videoAspect / viewAspect), maxOf(1f, viewAspect / videoAspect), width / 2f, height / 2f)
        })
    }

    fun close() { session?.close() }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = crop(width, height)
    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        close()
        // Keep the texture alive until the vendor decoder has finished releasing it.
        WallpaperDecoder.handler.post { texture.release() }
        session = null
        return false
    }
    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
}

private class WallpaperSession(context: Context, texture: SurfaceTexture, onReady: (Int, Int) -> Unit) {
    private val closed = AtomicBoolean(false)
    private var player: MediaPlayer? = null
    private var output: Surface? = null
    private var prepared = false

    init {
        WallpaperDecoder.handler.post {
            if (!closed.get()) try {
                output = Surface(texture)
                val media = MediaPlayer()
                player = media
                context.resources.openRawResourceFd(R.raw.bigdog_home).use { file ->
                    media.setDataSource(file.fileDescriptor, file.startOffset, file.length)
                }
                media.setSurface(output)
                media.setVolume(0f, 0f)
                media.isLooping = true
                media.setOnPreparedListener {
                    prepared = true
                    if (!closed.get()) {
                        onReady(it.videoWidth, it.videoHeight)
                        it.start()
                        Log.i("BigDogWallpaper", "Silent motion wallpaper started")
                    }
                }
                media.setOnErrorListener { _, what, extra ->
                    Log.w("BigDogWallpaper", "Motion unavailable: $what/$extra; keeping still artwork")
                    close()
                    true
                }
                media.prepareAsync()
            } catch (error: Exception) {
                Log.w("BigDogWallpaper", "Keeping still artwork", error)
                close()
            }
        }
    }

    fun close() {
        if (!closed.compareAndSet(false, true)) return
        WallpaperDecoder.handler.post {
            val old = player
            player = null
            try {
                old?.setOnPreparedListener(null)
                old?.setOnErrorListener(null)
                if (old != null) {
                    Log.i("BigDogWallpaper", "Stopping motion decoder")
                    if (prepared) {
                        old.pause()
                        old.isLooping = false
                    }
                    if (prepared) old.stop()
                }
            } catch (error: Exception) {
                Log.w("BigDogWallpaper", "Motion decoder stop failed; releasing", error)
            } finally {
                runCatching { old?.release() }.onFailure {
                    Log.w("BigDogWallpaper", "Motion decoder release failed", it)
                }
                output?.release()
                output = null
                if (old != null) Log.i("BigDogWallpaper", "Motion decoder released")
            }
        }
    }
}
