package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tv
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstalledTvApp
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
 * - Left: Hero tile (440x280dp, corner 20dp, green gradient + real installed app banner/icon
 *   when present, 3dp accent focus ring, title at 30sp, subtitle, and round 52dp accent play button).
 * - Right: Customizable Watch panel (Continue / Last Played, Next episode, To Watch / Favorites)
 *   over a 3x2 grid of 16:9 thumbnails using the default app thumbnails already installed on the TV.
 */
@Composable
fun B2HeroAndWatchShelf(
    heroApp: InstalledTvApp?,
    heroDisplayTitle: String,
    heroDisplaySubtitle: String,
    selectedTab: WatchTab,
    watchItems: List<WatchItem>,
    heroFocusRequester: FocusRequester,
    onSelectTab: (WatchTab) -> Unit,
    onHeroClick: () -> Unit,
    onChangeHeroAppClick: () -> Unit,
    onWatchItemClick: (WatchItem) -> Unit,
    onOpenCustomizeWatchShelf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        B2HeroTile(
            heroApp = heroApp,
            title = heroDisplayTitle,
            subtitle = heroDisplaySubtitle,
            focusRequester = heroFocusRequester,
            onClick = onHeroClick,
            onChangeHeroAppClick = onChangeHeroAppClick,
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight()
        )

        B2WatchPanel(
            selectedTab = selectedTab,
            watchItems = watchItems,
            onSelectTab = onSelectTab,
            onWatchItemClick = onWatchItemClick,
            onOpenCustomizeWatchShelf = onOpenCustomizeWatchShelf,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun B2HeroTile(
    heroApp: InstalledTvApp?,
    title: String,
    subtitle: String,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
    onChangeHeroAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroShape = RoundedCornerShape(20.dp)
    val heroBrush = Brush.linearGradient(
        colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd),
        start = Offset(0f, 0f),
        end = Offset(900f, 600f)
    )

    Box(modifier = modifier) {
        TvFocusableSurface(
            itemId = "hero_movie_box_pro",
            shape = heroShape,
            restingBrush = heroBrush,
            focusScale = 1.03f,
            focusBorderWidth = 3.dp,
            restingBorder = BorderStroke(2.dp, B2Accent.copy(alpha = 0.55f)),
            focusRequester = focusRequester,
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            if (heroApp?.bannerBitmap != null) {
                Image(
                    bitmap = heroApp.bannerBitmap,
                    contentDescription = heroApp.name,
                    contentScale = ContentScale.Crop,
                    alpha = 0.42f,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    HeroGradientStart.copy(alpha = 0.45f),
                                    HeroGradientEnd.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
            } else {
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
            }

            if (heroApp?.iconBitmap != null) {
                Image(
                    bitmap = heroApp.iconBitmap,
                    contentDescription = heroApp.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = B2MainText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = B2MainText.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(B2Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Launch $title",
                        tint = B2AccentContent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Top-right button to customize which installed APK is featured on the Hero Tile
        TvFocusableSurface(
            itemId = "hero_change_app_btn",
            shape = RoundedCornerShape(14.dp),
            restingColor = Color.Black.copy(alpha = 0.38f),
            focusedColor = B2RaisedSurface,
            onClick = onChangeHeroAppClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .height(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SwapHoriz,
                    contentDescription = "Change featured Hero app",
                    tint = B2MainText.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Change app",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = B2MainText.copy(alpha = 0.85f)
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
    onOpenCustomizeWatchShelf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredItems = watchItems.filter { it.tab == selectedTab }.take(6)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                        shape = RoundedCornerShape(15.dp),
                        restingColor = if (isSelected) B2Accent else B2TileSurface,
                        focusedColor = if (isSelected) B2Accent else B2RaisedSurface,
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

                // Customize / Add item to the current tab (Continue, Next episode, or To Watch)
                TvFocusableSurface(
                    itemId = "watch_customize_button",
                    shape = RoundedCornerShape(15.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    onClick = onOpenCustomizeWatchShelf,
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
                            contentDescription = "Add to ${selectedTab.title}",
                            tint = B2Accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Add to ${selectedTab.title}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = B2Accent
                        )
                    }
                }
            }

            Text(
                text = "From your apps",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = B2QuietText
            )
        }

        val row1Items = filteredItems.take(3)
        val row2Items = filteredItems.drop(3).take(3)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WatchThumbnailRow(
                items = row1Items,
                onWatchItemClick = onWatchItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            WatchThumbnailRow(
                items = row2Items,
                onWatchItemClick = onWatchItemClick,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)
    val thumbBrush = Brush.verticalGradient(
        colors = listOf(item.gradientStart, item.gradientEnd)
    )

    TvFocusableSurface(
        itemId = item.id,
        shape = cardShape,
        restingBrush = if (item.bannerBitmap == null) thumbBrush else null,
        restingColor = B2TileSurface,
        focusScale = 1.05f,
        onClick = onClick,
        modifier = modifier
    ) {
        if (item.bannerBitmap != null) {
            Image(
                bitmap = item.bannerBitmap,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (item.iconBitmap != null) {
            Image(
                bitmap = item.iconBitmap,
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f)
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
 * Dynamic B2 Apps Row:
 * - Shows the user's selected Home apps (or top installed TV apps when none selected yet)
 *   using each app's own default banner or icon already installed on the TV.
 * - Clicking "All apps" opens the full list of all APKs and built-in TV apps where the user
 *   can select which apps are displayed on the Home screen under APPS.
 */
@Composable
fun B2DynamicAppsRowSection(
    installedApps: List<InstalledTvApp>,
    isLoading: Boolean,
    onAppClick: (InstalledTvApp) -> Unit,
    onAllAppsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinnedOnly = installedApps.filter { it.isPinned }
    val rowApps = if (pinnedOnly.isNotEmpty()) {
        pinnedOnly
    } else {
        installedApps.take(6)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "APPS",
            style = MaterialTheme.typography.labelMedium,
            color = B2QuietText
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(
                items = rowApps,
                key = { it.packageName }
            ) { app ->
                DynamicInstalledAppTile(
                    app = app,
                    onClick = { onAppClick(app) },
                    modifier = Modifier
                        .width(144.dp)
                        .height(92.dp)
                )
            }

            // Trailing dashed "All apps" tile matching B2 spec (144x92dp)
            item(key = "app_all_apps_dashed") {
                TvFocusableSurface(
                    itemId = "app_all_apps",
                    shape = RoundedCornerShape(16.dp),
                    restingColor = Color.Transparent,
                    focusedColor = B2RaisedSurface,
                    dashedOutlineCornerRadius = 16.dp,
                    onClick = onAllAppsClick,
                    modifier = Modifier
                        .width(144.dp)
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
                                .background(B2TileSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Apps,
                                contentDescription = "All apps",
                                tint = B2QuietText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isLoading) "Loading..." else "All apps",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
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

@Composable
fun DynamicInstalledAppTile(
    app: InstalledTvApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showPinBadge: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    TvFocusableSurface(
        itemId = app.id,
        shape = shape,
        restingColor = B2TileSurface,
        focusedColor = B2RaisedSurface,
        onClick = onClick,
        modifier = modifier
    ) {
        if (app.bannerBitmap != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = app.bannerBitmap,
                    contentDescription = app.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.72f)
                                )
                            )
                        )
                )
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = B2MainText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(B2RaisedSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tv,
                            contentDescription = app.name,
                            tint = B2Accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = B2MainText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showPinBadge && app.isPinned) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(B2Accent)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "On Home",
                    tint = B2AccentContent,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = B2AccentContent
                )
            }
        }
    }
}

@Composable
fun B2InputsRowSection(
    inputs: List<TvInputPillItem>,
    onInputClick: (TvInputPillItem) -> Unit,
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
                    shape = RoundedCornerShape(14.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
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
