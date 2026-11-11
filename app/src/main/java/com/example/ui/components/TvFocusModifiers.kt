package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.B2Accent
import com.example.ui.theme.B2DividerBorder
import com.example.ui.theme.B2RaisedSurface
import com.example.ui.theme.B2TileSurface

/**
 * Core Android TV D-Pad Focusable Surface following the B2 Design Spec:
 * - Scales by 1.06x when focused using a lightweight spring animation
 * - Draws a crisp #8FD6B0 accent focus ring when focused
 * - Supports both D-Pad Center/Enter keys on physical remotes and pointer clicks
 */
@Composable
fun TvFocusableSurface(
    itemId: String,
    itemLabel: String = itemId,
    shape: Shape,
    onFocused: (String, String) -> Unit = { _, _ -> },
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    restingColor: Color = B2TileSurface,
    focusedColor: Color = B2RaisedSurface,
    restingBrush: Brush? = null,
    focusScale: Float = 1.06f,
    focusBorderWidth: Dp = 2.5.dp,
    restingBorder: BorderStroke? = null,
    dashedOutlineCornerRadius: Dp? = null,
    focusRequester: FocusRequester? = null,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val keyScope = rememberCoroutineScope()
    var holdJob by remember { mutableStateOf<Job?>(null) }
    var held by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { holdJob?.cancel() } }
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tv_focus_scale"
    )

    val baseModifier = if (focusRequester != null) {
        modifier.focusRequester(focusRequester)
    } else {
        modifier
    }

    Box(
        modifier = baseModifier
            .testTag(itemId)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { state ->
                val nowFocused = state.isFocused
                if (isFocused != nowFocused) {
                    if (!nowFocused) { holdJob?.cancel(); holdJob = null }
                    isFocused = nowFocused
                    if (nowFocused) {
                        onFocused(itemId, itemLabel)
                    }
                }
            }
            .onKeyEvent { event ->
                val native = event.nativeKeyEvent
                val isSelectKey = native.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    native.keyCode == KeyEvent.KEYCODE_ENTER ||
                    native.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                if (isSelectKey && onLongClick != null) {
                    if (native.action == KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                        held = false
                        holdJob?.cancel()
                        holdJob = keyScope.launch {
                            delay(android.view.ViewConfiguration.getLongPressTimeout().toLong())
                            held = true
                            onLongClick()
                        }
                    } else if (native.action == KeyEvent.ACTION_DOWN && native.isLongPress && !held) {
                        holdJob?.cancel()
                        held = true
                        onLongClick()
                    } else if (native.action == KeyEvent.ACTION_UP) {
                        holdJob?.cancel()
                        holdJob = null
                        if (!held && !native.isCanceled) onClick()
                    }
                    true
                } else if (isSelectKey && native.action == KeyEvent.ACTION_UP) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (dashedOutlineCornerRadius != null && !isFocused) {
                    Modifier.drawBehind {
                        val strokeWidthPx = 1.5.dp.toPx()
                        val dashPx = 6.dp.toPx()
                        val cornerPx = dashedOutlineCornerRadius.toPx()
                        drawRoundRect(
                            color = B2DividerBorder,
                            style = Stroke(
                                width = strokeWidthPx,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, dashPx), 0f)
                            ),
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    }
                } else {
                    Modifier
                }
            )
            .then(
                when {
                    isFocused -> Modifier.border(focusBorderWidth, B2Accent, shape)
                    restingBorder != null -> Modifier.border(restingBorder, shape)
                    else -> Modifier
                }
            )
            .clip(shape)
            .then(
                if (restingBrush != null) {
                    Modifier.background(restingBrush)
                } else {
                    Modifier.background(if (isFocused) focusedColor else restingColor)
                }
            )
    ) {
        content(isFocused)
    }
}
