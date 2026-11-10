package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WallpaperPreset
import com.example.model.WallpaperSettings
import com.example.ui.theme.B2Accent
import com.example.ui.theme.B2AccentContent
import com.example.ui.theme.B2Background
import com.example.ui.theme.B2DividerBorder
import com.example.ui.theme.B2MainText
import com.example.ui.theme.B2QuietText
import com.example.ui.theme.B2RaisedSurface
import com.example.ui.theme.B2TileSurface
import kotlinx.coroutines.launch

/**
 * Renders the active wallpaper behind the entire launcher (still preset, animated drifting
 * gradient preset, or custom user image) with the user's configured Dim and Blur levels.
 */
@Composable
fun B2WallpaperBackdrop(
    settings: WallpaperSettings,
    customBitmap: ImageBitmap?,
    modifier: Modifier = Modifier,
    animationAllowed: Boolean = true
) {
    val resumed = rememberLauncherResumed()
    val blurModifier = when (settings.blurLevel) {
        1 -> Modifier.blur(6.dp)
        2 -> Modifier.blur(14.dp)
        3 -> Modifier.blur(24.dp)
        else -> Modifier
    }

    Box(modifier = modifier.fillMaxSize().background(B2Background)) {
        if (customBitmap != null && !settings.customImageUri.isNullOrBlank()) {
            Image(
                bitmap = customBitmap,
                contentDescription = "Custom Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
        } else {
            PresetWallpaperCanvas(
                preset = settings.preset,
                animate = settings.animationEnabled && animationAllowed && resumed,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
        }

        // Dim overlay
        if (settings.dimFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.dimFraction.coerceIn(0f, 0.85f)))
            )
        }
    }
}

@Composable
private fun PresetWallpaperCanvas(
    preset: WallpaperPreset,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    if (!preset.isAnimated || !animate) {
        WallpaperArtwork(preset, 0f, modifier)
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wallpaper_drift")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    WallpaperArtwork(preset, phase, modifier)
}

/**
 * Full-Screen Wallpaper Studio matching the B2 Wallpaper Screen spec:
 * - Shows a full-screen preview of the selected wallpaper with a faint preview of the home layout over it.
 * - All six presets and Custom photo fit in a two-row grid, with a "Live" badge on animated ones:
 *   Aurora, Deep space, and Slow waves (animated); Charcoal, Sunset, and Forest mist (still).
 * - A dashed card lets the user add their own photo/image.
 * - Dim and Blur steppers and a "Set wallpaper" button sit above the grid.
 */
@Composable
fun B2WallpaperStudioScreen(
    currentSettings: WallpaperSettings,
    customBitmap: ImageBitmap?,
    onApplyWallpaper: (WallpaperSettings) -> Unit,
    onPickCustomImage: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draftPreset by remember(currentSettings) { mutableStateOf(currentSettings.preset) }
    var draftCustomUri by remember(currentSettings) { mutableStateOf(currentSettings.customImageUri) }
    var draftDim by remember(currentSettings) { mutableFloatStateOf(currentSettings.dimFraction) }
    var draftBlur by remember(currentSettings) { mutableIntStateOf(currentSettings.blurLevel) }
    var draftMotion by remember(currentSettings) { mutableStateOf(currentSettings.animationEnabled) }
    val firstPresetFocusRequester = remember { FocusRequester() }
    val motionFocusRequester = remember { FocusRequester() }
    val studioScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        firstPresetFocusRequester.requestFocus()
    }

    val previewSettings = WallpaperSettings(
        preset = draftPreset,
        customImageUri = draftCustomUri,
        dimFraction = draftDim,
        blurLevel = draftBlur,
        animationEnabled = draftMotion
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Live preview of the draft wallpaper
        B2WallpaperBackdrop(
            settings = previewSettings,
            customBitmap = customBitmap
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Wallpaper & Appearance",
                        style = MaterialTheme.typography.headlineMedium,
                        color = B2MainText
                    )
                    Text(
                        text = "Previewing ${if (draftCustomUri != null) "Custom Image" else draftPreset.title} · Adjust Dim and Blur below",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = B2QuietText
                    )
                }

                TvFocusableSurface(
                    itemId = "wallpaper_back_btn",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface.copy(alpha = 0.85f),
                    focusedColor = B2RaisedSurface,
                    onClick = onBackToHome,
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back to Home",
                            style = MaterialTheme.typography.bodyMedium,
                            color = B2MainText
                        )
                    }
                }
            }

            // Faint wireframe preview of the B2 Home layout in the center
            FaintHomeLayoutPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 18.dp)
            )

            // Bottom Controls: Dim & Blur steppers + Set wallpaper button above the 152x86dp preset row
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dim control pill
                        WallpaperStepperControl(
                            label = "Dim",
                            valueText = "${(draftDim * 100).toInt()}%",
                            onDecrease = { draftDim = (draftDim - 0.10f).coerceAtLeast(0f) },
                            onIncrease = { draftDim = (draftDim + 0.10f).coerceAtMost(0.80f) },
                            decId = "wallpaper_dim_dec",
                            incId = "wallpaper_dim_inc"
                        )

                        // Blur control pill
                        val blurLabel = when (draftBlur) {
                            0 -> "Off"
                            1 -> "Subtle"
                            2 -> "Medium"
                            else -> "High"
                        }
                        WallpaperStepperControl(
                            label = "Blur",
                            valueText = blurLabel,
                            onDecrease = { draftBlur = (draftBlur - 1).coerceAtLeast(0) },
                            onIncrease = { draftBlur = (draftBlur + 1).coerceAtMost(3) },
                            decId = "wallpaper_blur_dec",
                            incId = "wallpaper_blur_inc"
                        )
                        TvFocusableSurface(
                            itemId = "wallpaper_motion_toggle",
                            focusRequester = motionFocusRequester,
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                draftMotion = !draftMotion
                                studioScope.launch { androidx.compose.runtime.withFrameNanos { }; motionFocusRequester.requestFocus() }
                            },
                            modifier = Modifier.height(42.dp)
                        ) {
                            Text("Motion: ${if (draftMotion) "On" else "Off"}",
                                color = B2MainText, style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center).padding(horizontal = 14.dp))
                        }
                    }

                    // Set wallpaper button
                    TvFocusableSurface(
                        itemId = "wallpaper_apply_button",
                        shape = RoundedCornerShape(14.dp),
                        restingColor = B2Accent,
                        focusedColor = B2Accent,
                        onClick = { onApplyWallpaper(previewSettings) },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = B2AccentContent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Set wallpaper",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = B2AccentContent
                            )
                        }
                    }
                }

                // Fixed grid: no cropped edge cards or off-screen carousel items.
                val orderedPresets = listOf(
                    WallpaperPreset.AURORA,
                    WallpaperPreset.DEEP_SPACE,
                    WallpaperPreset.SLOW_WAVES,
                    WallpaperPreset.CHARCOAL,
                    WallpaperPreset.SUNSET,
                    WallpaperPreset.FOREST_MIST
                )

                val options: List<WallpaperPreset?> = orderedPresets + listOf<WallpaperPreset?>(null)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    options.chunked(4).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { preset ->
                                val cardModifier = Modifier.weight(1f).height(78.dp)
                                if (preset != null) {
                                    WallpaperPresetCard(
                                        preset = preset,
                                        isSelected = draftCustomUri == null && draftPreset == preset,
                                        focusRequester = if (preset == orderedPresets.first()) firstPresetFocusRequester else null,
                                        modifier = cardModifier,
                                        onClick = {
                                            draftCustomUri = null
                                            draftPreset = preset
                                        }
                                    )
                                } else {
                                    CustomWallpaperCard(onPickCustomImage, cardModifier)
                                }
                            }
                            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperPresetCard(
    preset: WallpaperPreset,
    isSelected: Boolean,
    focusRequester: FocusRequester?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val brush = Brush.linearGradient(colors = preset.colors)
    val localFocus = remember { FocusRequester() }
    val cardFocus = focusRequester ?: localFocus
    val scope = rememberCoroutineScope()

    TvFocusableSurface(
        itemId = "wallpaper_preset_${preset.id}",
        shape = shape,
        restingBrush = brush,
        focusRequester = cardFocus,
        focusScale = 1.025f,
        onClick = {
            onClick()
            scope.launch { androidx.compose.runtime.withFrameNanos { }; cardFocus.requestFocus() }
        },
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // The thumbnail uses the same artwork as Home, frozen to avoid six
            // extra animation loops while browsing the preset row.
            WallpaperArtwork(preset, 0f, Modifier.fillMaxSize())
            // Live badge on animated presets
            if (preset.isAnimated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(B2Accent.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = B2AccentContent
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = B2MainText
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = B2Accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomWallpaperCard(onPick: () -> Unit, modifier: Modifier) {
    TvFocusableSurface(
        itemId = "wallpaper_add_custom_card",
        shape = RoundedCornerShape(14.dp),
        restingColor = B2TileSurface.copy(alpha = 0.65f),
        focusedColor = B2RaisedSurface,
        dashedOutlineCornerRadius = 14.dp,
        focusScale = 1.025f,
        onClick = onPick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = "Add custom wallpaper",
                tint = B2Accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text("Custom photo", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = B2MainText)
        }
    }
}

@Composable
private fun WallpaperStepperControl(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decId: String,
    incId: String
) {
    val decreaseFocus = remember { FocusRequester() }
    val increaseFocus = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    fun adjust(action: () -> Unit, focus: FocusRequester) {
        action()
        scope.launch {
            androidx.compose.runtime.withFrameNanos { }
            focus.requestFocus()
        }
    }
    Row(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(B2TileSurface.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = B2QuietText
        )
        TvFocusableSurface(
            itemId = decId,
            focusRequester = decreaseFocus,
            shape = RoundedCornerShape(8.dp),
            restingColor = B2RaisedSurface,
            focusedColor = B2RaisedSurface,
            onClick = { adjust(onDecrease, decreaseFocus) },
            modifier = Modifier.size(28.dp)
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.bodyMedium,
                color = B2MainText,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = B2MainText,
            modifier = Modifier.width(52.dp)
        )
        TvFocusableSurface(
            itemId = incId,
            focusRequester = increaseFocus,
            shape = RoundedCornerShape(8.dp),
            restingColor = B2RaisedSurface,
            focusedColor = B2RaisedSurface,
            onClick = { adjust(onIncrease, increaseFocus) },
            modifier = Modifier.size(28.dp)
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.bodyMedium,
                color = B2MainText,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun FaintHomeLayoutPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, B2DividerBorder.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(7) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
            }
        }
    }
}
