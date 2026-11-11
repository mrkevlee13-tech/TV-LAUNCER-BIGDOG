package com.example.ui.components

import android.media.tv.TvContract
import android.media.tv.TvView
import android.media.AudioManager
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun sourceIcon(type: HardwareInputType?): ImageVector = when (type) {
    HardwareInputType.USB -> Icons.Outlined.Usb
    HardwareInputType.SCREEN_CAST -> Icons.Outlined.Cast
    HardwareInputType.AV -> Icons.Outlined.SettingsInputComposite
    else -> Icons.Outlined.SettingsInputHdmi
}

@Composable
fun B2SourceShelf(
    source: HeroSource?,
    heroApp: InstalledTvApp?,
    vlcPlayback: VlcPlayback?,
    inputs: List<TvInputPillItem>,
    isDraft: Boolean,
    heroFocusRequester: FocusRequester,
    inputsFocusRequester: FocusRequester,
    onSelectInput: (TvInputPillItem) -> Unit,
    onOpenHero: () -> Unit,
    onChooseApp: () -> Unit,
    previewEnabled: Boolean,
    heroCardScale: Float = 1f
) {
    val lastVlcVideo = vlcPlayback?.takeIf { source?.kind == "app" && source.target == "org.videolan.vlc" }
    val input = inputs.firstOrNull { source?.kind == "input" && it.id == source.target }
    val scope = rememberCoroutineScope()
    val chooseAppFocus = remember { FocusRequester() }
    Row(Modifier.fillMaxWidth().height(280.dp * maxOf(1f, heroCardScale)), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(Modifier.weight(1.15f * heroCardScale).height(280.dp * heroCardScale)) {
            TvFocusableSurface(
                itemId = "hero_source",
                itemLabel = source?.title ?: "Choose an app or input",
                shape = RoundedCornerShape(20.dp),
                restingColor = Color(0xFF13252A),
                focusScale = 1.025f,
                restingBorder = BorderStroke(1.dp, B2Accent.copy(alpha = 0.4f)),
                focusRequester = heroFocusRequester,
                onClick = onOpenHero,
                modifier = Modifier.fillMaxSize().focusProperties {
                    up = chooseAppFocus
                    right = inputsFocusRequester
                }
            ) {
                val hardware = input != null && input.inputType !in listOf(HardwareInputType.USB, HardwareInputType.SCREEN_CAST)
                if (hardware) {
                    InputVideoSurface(input, previewEnabled, false, {}, Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF183F45), Color(0xFF0C171F)))))
                    if (source?.kind == "app" && heroApp?.iconBitmap != null) {
                        Image(heroApp.iconBitmap, heroApp.name, Modifier.align(Alignment.Center).size(80.dp * heroCardScale))
                    } else {
                        Icon(if (source?.kind == "media") Icons.Outlined.Movie else if (input != null) sourceIcon(input.inputType) else Icons.Outlined.Tv,
                            null, Modifier.align(Alignment.Center).size(80.dp * heroCardScale), tint = B2Accent.copy(alpha = 0.7f))
                    }
                }
                Column(Modifier.align(Alignment.TopStart).padding(20.dp)) {
                    Text(if (isDraft) "SELECTED SOURCE" else "LAST USED", color = B2Accent,
                        style = MaterialTheme.typography.labelMedium)
                }
                Row(Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
                    .padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(lastVlcVideo?.title ?: source?.title ?: "Choose an app or input", color = B2MainText,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = (26f * heroCardScale).sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val hint = when {
                            lastVlcVideo != null -> if (lastVlcVideo.isPlaying)
                                "VLC · Playing · OK for video" else "VLC · Paused / last video · OK to resume"
                            source?.kind == "media" -> "USB video · OK to open in your player"
                            input?.inputType == HardwareInputType.USB -> if (input.isConnected)
                                "${input.status} · OK to choose a video" else "Connect a USB drive first"
                            input?.inputType == HardwareInputType.SCREEN_CAST -> "OK to open SmartShare"
                            input != null -> "${input.label} · ${input.status} · OK for full screen"
                            source != null -> if (isDraft) "App · OK to open" else "Last opened app · OK to open"
                            else -> "Select a source on the right"
                        }
                        Text(hint, color = B2QuietText, fontSize = 12.sp, maxLines = 2)
                    }
                    Icon(Icons.Filled.PlayArrow, "Open selected source", Modifier.size(34.dp), tint = B2Accent)
                }
            }
            TvFocusableSurface(
                itemId = "hero_choose_app", shape = RoundedCornerShape(12.dp),
                focusRequester = chooseAppFocus,
                restingColor = Color.Black.copy(alpha = 0.6f), onClick = onChooseApp,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).height(32.dp).focusProperties {
                    down = heroFocusRequester
                    right = inputsFocusRequester
                }
            ) { Text("Choose app", color = B2MainText, fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp)) }
        }
        Column(Modifier.weight(1f).height(280.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("INPUTS", color = B2QuietText, style = MaterialTheme.typography.labelMedium)
            inputs.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { item ->
                        TvFocusableSurface(
                            itemId = "source_${item.id}", itemLabel = "${item.label}: ${item.status}",
                            focusRequester = if (item.id == inputs.firstOrNull()?.id) inputsFocusRequester else null,
                            shape = RoundedCornerShape(14.dp), focusScale = 1.025f,
                            restingColor = if (item.id == input?.id) Color(0xFF203C3D) else B2TileSurface,
                            onClick = {
                                onSelectInput(item)
                                scope.launch { withFrameNanos { }; heroFocusRequester.requestFocus() }
                            }, modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Icon(sourceIcon(item.inputType), null, Modifier.size(30.dp),
                                    tint = if (item.isConnected) B2Accent else B2QuietText)
                                Spacer(Modifier.height(5.dp))
                                Text(item.label, color = B2MainText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(item.status, color = if (item.isConnected) B2Accent else B2QuietText,
                                    fontSize = 9.sp, maxLines = 2)
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** One native TV session at a time. No vendor packages are enabled to obtain it. */
@Composable
private fun InputVideoSurface(
    input: TvInputPillItem,
    enabled: Boolean,
    fullScreen: Boolean,
    onVideoAvailable: () -> Unit,
    modifier: Modifier
) {
    val resumed = rememberLauncherResumed()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val inputId = input.tvInputId
    val active = enabled && resumed && input.isConnected && inputId != null
    var status by remember(inputId, active) { mutableStateOf(if (active) "Connecting preview…" else "${input.status} · Turn on the source") }
    var videoAvailable by remember(inputId, active) { mutableStateOf(false) }
    val latestVideoAvailable by rememberUpdatedState(onVideoAvailable)
    Box(modifier.background(Color.Black)) {
        if (active) {
            val view = remember(inputId) {
                TvView(context).apply {
                    isFocusable = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                }
            }
            AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
            DisposableEffect(view, fullScreen, lifecycleOwner) {
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                var tuned = false
                var ownsAudioFocus = false
                fun release() {
                    // Mute before releasing the hardware session; disposal alone can be too late.
                    runCatching { view.setStreamVolume(0f) }
                    runCatching { view.reset() }
                    tuned = false
                    videoAvailable = false
                }
                val audioFocusListener = AudioManager.OnAudioFocusChangeListener { change ->
                    if (change <= AudioManager.AUDIOFOCUS_LOSS) {
                        ownsAudioFocus = false
                        release()
                        status = "HDMI paused while another app plays"
                    }
                }
                view.setCallback(object : TvView.TvInputCallback() {
                    override fun onVideoAvailable(id: String) {
                        videoAvailable = true
                        status = "Live preview"
                        Log.i("BigDogInputs", "Video available: $id fullscreen=$fullScreen")
                        view.setStreamVolume(if (fullScreen && ownsAudioFocus) 1f else 0f)
                        if (fullScreen) latestVideoAvailable()
                    }
                    override fun onVideoUnavailable(id: String, reason: Int) {
                        videoAvailable = false
                        status = "No video signal · Check the source / soundbar input"
                        Log.i("BigDogInputs", "Video unavailable: $id reason=$reason")
                    }
                    override fun onConnectionFailed(id: String) {
                        status = "Preview unavailable on this TV"
                        Log.w("BigDogInputs", "Input session connection failed: $id")
                    }
                    override fun onDisconnected(id: String) {
                        videoAvailable = false
                        status = "Source disconnected"
                    }
                })
                fun tune() {
                    if (tuned) return
                    runCatching {
                        if (fullScreen) {
                            @Suppress("DEPRECATION")
                            val result = audio.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN)
                            ownsAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                            if (!ownsAudioFocus) { status = "HDMI audio is in use by another app"; return }
                        }
                        view.setStreamVolume(0f)
                        view.tune(inputId, TvContract.buildChannelUriForPassthroughInput(inputId))
                        tuned = true
                    }.onFailure {
                        status = "TV blocked this input preview"
                        Log.w("BigDogInputs", "Unable to tune $inputId", it)
                    }
                }
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) tune()
                    if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                        release()
                        @Suppress("DEPRECATION")
                        audio.abandonAudioFocus(audioFocusListener)
                        ownsAudioFocus = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) tune()
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    release()
                    @Suppress("DEPRECATION")
                    audio.abandonAudioFocus(audioFocusListener)
                    runCatching { view.setCallback(null) }
                }
            }
            LaunchedEffect(inputId) {
                delay(8_000)
                if (!videoAvailable) status = "No preview picture · Check the source / soundbar input"
            }
        }
        if (!videoAvailable) {
            Column(Modifier.align(Alignment.Center).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(sourceIcon(input.inputType), null, Modifier.size(48.dp), tint = B2QuietText)
                Text(status, color = B2MainText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun B2InputFullScreen(input: TvInputPillItem, onBack: () -> Unit, onVideoAvailable: () -> Unit) {
    val backFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { withFrameNanos { }; backFocus.requestFocus() }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        InputVideoSurface(input, true, true, onVideoAvailable, Modifier.fillMaxSize())
        TvFocusableSurface(
            itemId = "input_fullscreen_back", shape = RoundedCornerShape(12.dp),
            restingColor = Color.Black.copy(alpha = 0.65f), focusRequester = backFocus, onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(24.dp).height(42.dp)
        ) { Text("Back to Home · ${input.label}", color = B2MainText,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp)) }
    }
}
