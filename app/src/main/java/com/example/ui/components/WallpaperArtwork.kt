package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.model.WallpaperPreset
import kotlin.math.sin

/** Resolution-independent scenes shared by Home, the studio and still thumbnails. */
@Composable
internal fun WallpaperArtwork(preset: WallpaperPreset, phase: Float, modifier: Modifier) {
    Canvas(modifier) { drawWallpaperScene(preset, phase) }
}

private fun DrawScope.drawWallpaperScene(preset: WallpaperPreset, phase: Float) {
    val w = size.width
    val h = size.height
    fun sky(vararg colors: Long) = drawRect(Brush.verticalGradient(colors.map { Color(it) }))
    fun glow(x: Float, y: Float, radius: Float, color: Color) {
        val center = Offset(w * x, h * y)
        drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), center, w * radius),
            w * radius, center)
    }
    fun ridge(y: Float, amplitude: Float, seed: Float, color: Color) {
        val path = Path().apply {
            moveTo(0f, h)
            for (i in 0..40) {
                val x = i / 40f
                val height = y + amplitude * (sin(x * 9f + seed) + 0.4f * sin(x * 21f + seed))
                lineTo(x * w, height * h)
            }
            lineTo(w, h)
            close()
        }
        drawPath(path, color)
    }
    when (preset) {
        WallpaperPreset.CHARCOAL -> {
            sky(0xFF171D25, 0xFF0A0E14)
            glow(0.75f, 0.05f, 0.7f, Color(0xFF34424D).copy(alpha = 0.5f))
            repeat(7) { index ->
                val x = (index - 2) * w * 0.25f
                val path = Path().apply {
                    moveTo(x, h); lineTo(x + w * 0.55f, 0f)
                    lineTo(x + w * 0.78f, 0f); lineTo(x + w * 0.23f, h); close()
                }
                drawPath(path, Color.White.copy(alpha = if (index % 2 == 0) 0.018f else 0.008f))
                drawLine(Color.White.copy(alpha = 0.025f), Offset(x, h), Offset(x + w * 0.55f, 0f), 1f)
            }
        }
        WallpaperPreset.AURORA -> {
            sky(0xFF060E24, 0xFF102A37, 0xFF071B20)
            glow(0.68f, 0.20f, 0.55f, Color(0xFF3A538C).copy(alpha = 0.4f))
            repeat(4) { band ->
                val drift = sin(phase) * 0.035f
                val top = 0.12f + band * 0.06f
                val ribbon = Path().apply {
                    moveTo(-w * 0.1f, h * (top + 0.25f))
                    cubicTo(w * 0.2f, h * (top - 0.2f + drift), w * 0.45f, h * (top + 0.6f), w * 1.1f, h * (top - 0.02f))
                    lineTo(w * 1.1f, h * (top + 0.2f))
                    cubicTo(w * 0.5f, h * (top + 0.85f), w * 0.3f, h * (top + drift), -w * 0.1f, h * (top + 0.4f))
                    close()
                }
                drawPath(ribbon, Brush.verticalGradient(listOf(Color.Transparent,
                    Color(if (band % 2 == 0) 0xFF49DBB2 else 0xFF5A8ACB).copy(alpha = 0.20f), Color.Transparent)))
            }
            stars(44, 0.48f)
            ridge(0.89f, 0.035f, 2f, Color(0xFF06131D))
        }
        WallpaperPreset.DEEP_SPACE -> {
            sky(0xFF070A1B, 0xFF171329, 0xFF080D1B)
            glow(0.67f + sin(phase) * 0.025f, 0.32f, 0.43f, Color(0xFF653C87).copy(alpha = 0.45f))
            glow(0.38f, 0.64f, 0.37f, Color(0xFF215A84).copy(alpha = 0.32f))
            stars(100, 0.95f)
            val center = Offset(w * 0.84f, h * 0.82f)
            val radius = h * 0.27f
            drawCircle(Color(0xFF415F86).copy(alpha = 0.4f), radius * 1.018f, center)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF304365), Color(0xFF0B1023), Color(0xFF050916)),
                center - Offset(radius * 0.55f, radius * 0.6f), radius * 1.7f), radius, center)
        }
        WallpaperPreset.SLOW_WAVES -> {
            sky(0xFF102E42, 0xFF184C5B, 0xFF0A202F)
            glow(0.8f, 0.15f, 0.6f, Color(0xFF6AA4A2).copy(alpha = 0.25f))
            repeat(5) { layer ->
                val wave = Path().apply {
                    moveTo(0f, h)
                    for (i in 0..48) {
                        val x = i / 48f
                        lineTo(w * x, h * (0.36f + layer * 0.13f + 0.07f *
                            sin(x * 6.28f + phase + layer * 0.8f)))
                    }
                    lineTo(w, h); close()
                }
                drawPath(wave, Brush.verticalGradient(listOf(
                    Color(0xFF477E89).copy(alpha = 0.42f), Color(0xFF071B2B).copy(alpha = 0.85f))))
            }
        }
        WallpaperPreset.SUNSET -> {
            sky(0xFF27203E, 0xFF9A535D, 0xFFCF8C6A, 0xFF372F45)
            glow(0.73f, 0.53f, 0.34f, Color(0xFFFFCE8C).copy(alpha = 0.4f))
            drawCircle(Color(0xFFFFCC96).copy(alpha = 0.85f), h * 0.095f, Offset(w * 0.73f, h * 0.53f))
            ridge(0.68f, 0.045f, 2.5f, Color(0xFF68506A))
            ridge(0.78f, 0.055f, 0.8f, Color(0xFF413D59))
            ridge(0.93f, 0.075f, 4f, Color(0xFF202A3C))
        }
        WallpaperPreset.FOREST_MIST -> {
            sky(0xFF253F45, 0xFF66817D, 0xFF1B3836)
            glow(0.72f, 0.18f, 0.6f, Color(0xFFBED0C0).copy(alpha = 0.3f))
            ridge(0.45f, 0.07f, 2f, Color(0xFF526D6C))
            ridge(0.59f, 0.06f, 4f, Color(0xFF395858))
            repeat(3) { layer ->
                repeat(22) { index ->
                    val x = w * ((index + (layer % 2) * 0.45f) / 21f)
                    val base = h * (0.74f + layer * 0.13f)
                    val height = h * (0.10f + layer * 0.035f + ((index * 7) % 5) * 0.015f)
                    val tree = Path().apply {
                        moveTo(x, base - height)
                        lineTo(x - height * 0.22f, base); lineTo(x + height * 0.22f, base); close()
                    }
                    drawPath(tree, Color(listOf(0xFF2B4B49, 0xFF203F3C, 0xFF112F2C)[layer]))
                }
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF9EB9AD).copy(alpha = 0.08f),
                    Color.Transparent)), topLeft = Offset(0f, h * (0.62f + layer * 0.1f)),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.2f))
            }
            ridge(0.94f, 0.035f, 2f, Color(0xFF0D2524))
        }
    }
}

private fun DrawScope.stars(count: Int, opacity: Float) {
    repeat(count) { index ->
        // Fixed positions: no random allocations or flicker during recomposition.
        val x = ((index * 197 + 47) % 997) / 997f
        val y = ((index * 113 + 73) % 631) / 631f
        drawCircle(Color(0xFFD1E5F4).copy(alpha = opacity * (0.2f + (index % 4) * 0.18f)),
            (size.height * if (index % 11 == 0) 0.0016f else 0.0008f).coerceAtLeast(0.45f),
            Offset(x * size.width, y * size.height))
    }
}
