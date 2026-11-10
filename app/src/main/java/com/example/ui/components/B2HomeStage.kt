package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTileItem
import com.example.model.TvInputPillItem
import com.example.model.WatchItem
import com.example.model.WatchTab
import com.example.ui.theme.B2Accent
import com.example.ui.theme.B2AccentContent
import com.example.ui.theme.B2MainText
import com.example.ui.theme.B2NotConnectedDot
import com.example.ui.theme.B2QuietText
import com.example.ui.theme.B2RaisedSurface
import com.example.ui.theme.B2TileSurface
import com.example.ui.theme.HeroCircleOverlay
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart

/**
 * Hero Tile + Watch Panel Row (Middle shelf of the B2 home screen):
 * - Left: Hero tile (440x280dp proportion, corner 20dp, green gradient, 3dp accent focus ring,
 *   Movie Box Pro at 30sp, subtitle, and round 52dp accent play button).
 * - Right: Watch panel with 30dp tall pill tabs (Continue, Next episode, To Watch) over a
 *   3x2 grid of 16:9 thumbnails (corner 12dp, bottom shade, title at 13sp, source app at 11sp,
 *   and 3dp tall accent progress bar).
 */
@Composable
fun B2HeroAndWatchShelf(
    selectedTab: WatchTab,
    watchItems: List<WatchItem>,
    heroFocusRequester: FocusRequester,
    onSelectTab: (WatchTab) -> Unit,
    onHeroClick: () -> Unit,
    onWatchItemClick: (WatchItem) -> Unit,
    onOpenAddToWatch: () -> Unit,
    onItemFocused: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Tile: 440x280dp on 1280x720 baseline
        B2HeroTile(
            focusRequester = heroFocusRequester,
            onFocused = onItemFocused,
            onClick = onHeroClick,
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight()
        )

        // Watch Panel: fills remaining width to the right of the Hero
        B2WatchPanel(
            selectedTab = selectedTab,
            watchItems = watchItems,
            onSelectTab = onSelectTab,
            onWatchItemClick = onWatchItemClick,
            onOpenAddToWatch = onOpenAddToWatch,
            onItemFocused = onItemFocused,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun B2HeroTile(
    focusRequester: FocusRequester,
    onFocused: (String, String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroShape = RoundedCornerShape(20.dp)
    val heroBrush = Brush.linearGradient(
        colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd),
        start = Offset(0f, 0f),
        end = Offset(900f, 600f)
    )

    TvFocusableSurface(
        itemId = "hero_movie_box_pro",
        itemLabel = "Hero Tile · Movie Box Pro",
        shape = heroShape,
        restingBrush = heroBrush,
        focusScale = 1.03f,
        focusBorderWidth = 3.dp,
        restingBorder = BorderStroke(2.dp, B2Accent.copy(alpha = 0.55f)),
        focusRequester = focusRequester,
        onFocused = onFocused,
        onClick = onClick,
        modifier = modifier
    ) {
        // Decorative translucent radial circles on the right side matching B2 mockup
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = HeroCircleOverlay,
                radius = size.height * 0.62f,
                center = Offset(size.width * 0.82f, size.height * 0.34f)
            )
            drawCircle(
                color = HeroCircleOverlay,
                radius = size.height * 0.36f,
                center = Offset(size.width * 0.72f, size.height * 0.65f)
            )
        }

        // Bottom content: Title at 30sp, Subtitle, and round 52dp accent play button
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Movie Box Pro",
                    style = MaterialTheme.typography.headlineLarge,
                    color = B2MainText
                )
                Text(
                    text = "Open your library · 6 titles waiting",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = B2MainText.copy(alpha = 0.78f)
                )
            }

            // Round 52dp accent play button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(B2Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Launch Movie Box Pro",
                    tint = B2AccentContent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun B2WatchPanel(
    selectedTab: WatchTab,
    watchItems: List<WatchItem>,
    onSelectTab: (WatchTab) -> Unit,
    onWatchItemClick: (WatchItem) -> Unit,
    onOpenAddToWatch: () -> Unit,
    onItemFocused: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredItems = watchItems.filter { it.tab == selectedTab }.take(6)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top row: Three 30dp tall pill tabs + "From your apps" / "+ Add to Watch" action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WatchTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    TvFocusableSurface(
                        itemId = tab.id,
                        itemLabel = "Watch Tab · ${tab.title}",
                        shape = RoundedCornerShape(15.dp),
                        restingColor = if (isSelected) B2Accent else B2TileSurface,
                        focusedColor = if (isSelected) B2Accent else B2RaisedSurface,
                        onFocused = onItemFocused,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier.height(30.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                ),
                                color = if (isSelected) B2AccentContent else B2MainText
                            )
                        }
                    }
                }

                if (selectedTab == WatchTab.TO_WATCH) {
                    TvFocusableSurface(
                        itemId = "watch_add_manual_button",
                        itemLabel = "Watch Panel · Add Manual To Watch Item",
                        shape = RoundedCornerShape(15.dp),
                        restingColor = B2TileSurface,
                        focusedColor = B2RaisedSurface,
                        onFocused = onItemFocused,
                        onClick = onOpenAddToWatch,
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add To Watch title",
                                tint = B2Accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Add title",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = B2Accent
                            )
                        }
                    }
                }
            }

            Text(
                text = "From your apps",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = B2QuietText
            )
        }

        // 3 columns x 2 rows of 16:9 thumbnails, corner 12dp
        val row1Items = filteredItems.take(3)
        val row2Items = filteredItems.drop(3).take(3)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WatchThumbnailRow(
                items = row1Items,
                onWatchItemClick = onWatchItemClick,
                onItemFocused = onItemFocused,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            WatchThumbnailRow(
                items = row2Items,
                onWatchItemClick = onWatchItemClick,
                onItemFocused = onItemFocused,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun WatchThumbnailRow(
    items: List<WatchItem>,
    onWatchItemClick: (WatchItem) -> Unit,
    onItemFocused: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (colIndex in 0 until 3) {
            val item = items.getOrNull(colIndex)
            if (item != null) {
                WatchThumbnailCard(
                    item = item,
                    onFocused = onItemFocused,
                    onClick = { onWatchItemClick(item) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun WatchThumbnailCard(
    item: WatchItem,
    onFocused: (String, String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)
    val thumbBrush = Brush.verticalGradient(
        colors = listOf(item.gradientStart, item.gradientEnd)
    )

    TvFocusableSurface(
        itemId = item.id,
        itemLabel = "${item.title} (${item.subtitle})",
        shape = cardShape,
        restingBrush = thumbBrush,
        focusScale = 1.05f,
        onFocused = onFocused,
        onClick = onClick,
        modifier = modifier
    ) {
        // Subtle bottom gradient scrim for legible text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = B2MainText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = B2MainText.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3dp tall accent progress bar along the bottom edge
        if (item.progress > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(item.progress.coerceIn(0.05f, 1f))
                        .background(B2Accent)
                )
            }
        }
    }
}

/**
 * B2 Apps Row:
 * - Label APPS at 12sp in capitals
 * - Seven tiles of 144x92dp (scaled fluidly across the row) on #1A1D21
 * - Each shows the app's own banner/icon and its name at 13sp
 * - The last tile, All apps, has a dashed outline.
 */
@Composable
fun B2AppsRowSection(
    apps: List<AppTileItem>,
    onAppClick: (AppTileItem) -> Unit,
    onItemFocused: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "APPS",
            style = MaterialTheme.typography.labelMedium,
            color = B2QuietText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            apps.forEach { app ->
                val shape = RoundedCornerShape(16.dp)
                TvFocusableSurface(
                    itemId = app.id,
                    itemLabel = "App Tile · ${app.name}",
                    shape = shape,
                    restingColor = if (app.isAllAppsCard) Color.Transparent else B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    dashedOutlineCornerRadius = if (app.isAllAppsCard) 16.dp else null,
                    onFocused = onItemFocused,
                    onClick = { onAppClick(app) },
                    modifier = Modifier
                        .weight(1f)
                        .height(92.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(app.iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = appIconFor(app.id),
                                contentDescription = app.name,
                                tint = app.iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = if (app.isAllAppsCard) B2QuietText else B2MainText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun appIconFor(appId: String): ImageVector {
    return when (appId) {
        "app_vlc" -> Icons.Outlined.PlayCircleOutline
        "app_smarttube" -> Icons.Outlined.SmartDisplay
        "app_files" -> Icons.Outlined.Folder
        "app_vpn" -> Icons.Outlined.Shield
        "app_bilibili" -> Icons.Outlined.LiveTv
        "app_browser" -> Icons.Outlined.Language
        else -> Icons.Outlined.Apps
    }
}

/**
 * B2 Inputs Row:
 * - Label INPUTS at 12sp in capitals
 * - Six pills 58dp tall: HDMI 1, HDMI 2, HDMI 3, AV, USB and Screen cast
 * - Each has a status dot (accent #8FD6B0 when connected, #4A525C when not) and a status line.
 */
@Composable
fun B2InputsRowSection(
    inputs: List<TvInputPillItem>,
    onInputClick: (TvInputPillItem) -> Unit,
    onItemFocused: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "INPUTS",
            style = MaterialTheme.typography.labelMedium,
            color = B2QuietText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            inputs.forEach { input ->
                TvFocusableSurface(
                    itemId = input.id,
                    itemLabel = "Input · ${input.label} (${input.status})",
                    shape = RoundedCornerShape(14.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    onFocused = onItemFocused,
                    onClick = { onInputClick(input) },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (input.isConnected) B2Accent else B2NotConnectedDot)
                        )
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = input.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = B2MainText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = input.status,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = B2QuietText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
