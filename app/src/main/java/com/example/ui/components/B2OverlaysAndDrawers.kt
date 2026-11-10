package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.semantics.role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstalledTvApp
import com.example.model.QuickSettingRowItem
import com.example.model.QuickSettingTileItem
import com.example.model.WatchTab
import com.example.ui.theme.B2Accent
import com.example.ui.theme.B2AccentContent
import com.example.ui.theme.B2MainText
import com.example.ui.theme.B2QuietText
import com.example.ui.theme.B2QuickSettingsSurface
import com.example.ui.theme.B2RaisedSurface
import com.example.ui.theme.B2TileSurface

/**
 * B2 Quick Settings Slide-Over Panel:
 * - Slides in from the right over a dimmed home screen.
 * - 460dp wide on a #15181B background.
 * - Uses verticalScroll + explicit requiredHeight(80.dp) on the 2x2 tiles and requiredHeight(50.dp)
 *   on every TV settings row so no card (including Storage) is ever squashed on any TV resolution.
 */
@Composable
fun B2QuickSettingsOverlay(
    showLeftSidebar: Boolean,
    onShowLeftSidebarChange: (Boolean) -> Unit,
    onOpenSearch: () -> Unit,
    isOpen: Boolean,
    tiles: List<QuickSettingTileItem>,
    rows: List<QuickSettingRowItem>,
    onClose: () -> Unit,
    onSelectAction: (String, String) -> Unit,
    onOpenWallpaperStudio: () -> Unit
) {
    val sidebarToggleFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        LaunchedEffect(Unit) {
            runCatching { sidebarToggleFocusRequester.requestFocus() }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(460.dp)
                    .fillMaxHeight()
                    .background(B2QuickSettingsSurface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = 26.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = B2MainText
                    )

                    TvFocusableSurface(
                        itemId = "qs_close_button",
                        shape = CircleShape,
                        restingColor = B2TileSurface,
                        focusedColor = B2RaisedSurface,
                        onClick = onClose,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close Quick settings",
                            tint = B2MainText,
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Text(
                    text = "CUSTOM APP FUNCTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = B2QuietText
                )
                TvFocusableSurface(
                    itemId = "qs_show_left_sidebar",
                    focusRequester = sidebarToggleFocusRequester,
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    focusScale = 1.03f,
                    onClick = { onShowLeftSidebarChange(!showLeftSidebar) },
                    modifier = Modifier.fillMaxWidth().requiredHeight(62.dp).semantics {
                        role = Role.Switch
                        toggleableState = ToggleableState(showLeftSidebar)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Show left sidebar", color = B2MainText,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(if (showLeftSidebar) "On" else "Off - full width home",
                                color = B2QuietText, style = MaterialTheme.typography.labelSmall)
                        }
                        Switch(checked = showLeftSidebar, onCheckedChange = null)
                    }
                }
                QuickSettingRowCard(
                    row = QuickSettingRowItem(
                        id = "qs_row_search", title = "Search apps", settingsAction = ""
                    ),
                    onClick = onOpenSearch,
                    modifier = Modifier.fillMaxWidth().requiredHeight(50.dp)
                )

                val row1 = tiles.take(2)
                val row2 = tiles.drop(2).take(2)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row1.forEach { tile ->
                            QuickSettingTileCard(
                                tile = tile,
                                focusRequester = null,
                                onClick = { onSelectAction(tile.settingsAction, tile.title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .requiredHeight(80.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row2.forEach { tile ->
                            QuickSettingTileCard(
                                tile = tile,
                                focusRequester = null,
                                onClick = { onSelectAction(tile.settingsAction, tile.title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .requiredHeight(80.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "TV SETTINGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = B2QuietText
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        QuickSettingRowCard(
                            row = row,
                            onClick = {
                                if (row.opensWallpaperScreen) {
                                    onOpenWallpaperStudio()
                                } else {
                                    onSelectAction(row.settingsAction, row.title)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(50.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSettingTileCard(
    tile: QuickSettingTileItem,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableSurface(
        itemId = tile.id,
        shape = RoundedCornerShape(16.dp),
        restingColor = B2TileSurface,
        focusedColor = B2RaisedSurface,
        focusRequester = focusRequester,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (tile.isActive) B2Accent else B2RaisedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = quickTileIconFor(tile.id),
                    contentDescription = tile.title,
                    tint = if (tile.isActive) B2AccentContent else B2MainText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = B2MainText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = B2QuietText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickSettingRowCard(
    row: QuickSettingRowItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (row.isHighlightedAccent) B2Accent else B2MainText
    TvFocusableSurface(
        itemId = row.id,
        shape = RoundedCornerShape(12.dp),
        restingColor = B2TileSurface,
        focusedColor = B2RaisedSurface,
        focusScale = 1.03f,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = quickRowIconFor(row.id),
                    contentDescription = row.title,
                    tint = if (row.isHighlightedAccent) B2Accent else B2QuietText,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = row.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = if (row.isHighlightedAccent) B2Accent else B2QuietText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun quickTileIconFor(id: String): ImageVector {
    return when (id) {
        "qs_tile_wifi" -> Icons.Outlined.Wifi
        "qs_tile_bt" -> Icons.Outlined.Bluetooth
        "qs_tile_cast" -> Icons.Outlined.Cast
        else -> Icons.Outlined.Usb
    }
}

private fun quickRowIconFor(id: String): ImageVector {
    return when (id) {
        "qs_row_display" -> Icons.Outlined.Tv
        "qs_row_sound" -> Icons.AutoMirrored.Outlined.VolumeUp
        "qs_row_network" -> Icons.Outlined.Language
        "qs_row_storage" -> Icons.Outlined.Storage
        "qs_row_date" -> Icons.Outlined.Schedule
        "qs_row_wallpaper" -> Icons.Outlined.Wallpaper
        else -> Icons.Outlined.Tune
    }
}

/**
 * Full-Screen All Apps Drawer showing all sideloaded APKs and built-in TV apps with their real default banners/icons.
 * Lets the user select which apps are displayed on the Home screen under APPS ("Select Home Apps" mode),
 * set any app (e.g. Movie Box Pro) as the Hero Tile ("Set Hero App" mode), or launch any app immediately.
 */
@Composable
fun B2AllAppsOverlayModal(
    installedApps: List<InstalledTvApp>,
    onAppClick: (InstalledTvApp) -> Unit,
    onTogglePinApp: (InstalledTvApp) -> Unit,
    onSetHeroApp: (InstalledTvApp) -> Unit,
    onClose: () -> Unit
) {
    var mode by remember { mutableStateOf("LAUNCH") } // LAUNCH, PIN, HERO
    val closeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { closeFocusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.86f))
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(B2QuickSettingsSurface)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "All Installed Apps & APKs (${installedApps.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        color = B2MainText
                    )
                    Text(
                        text = when (mode) {
                            "PIN" -> "Select Home Apps: Press OK on any app to add/remove it from the Home screen APPS row"
                            "HERO" -> "Set Hero App: Press OK on any app (e.g. Movie Box Pro) to set it as the main Hero Tile"
                            else -> "Press OK on any app to open it, or click 'Select Home Apps' to choose which apps appear on Home"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = if (mode == "LAUNCH") B2QuietText else B2Accent
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableSurface(
                        itemId = "all_apps_pin_mode_btn",
                        shape = RoundedCornerShape(12.dp),
                        restingColor = if (mode == "PIN") B2Accent else B2TileSurface,
                        focusedColor = if (mode == "PIN") B2Accent else B2RaisedSurface,
                        onClick = { mode = if (mode == "PIN") "LAUNCH" else "PIN" },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (mode == "PIN") Icons.Outlined.Check else Icons.Outlined.PushPin,
                                contentDescription = "Select Home Apps",
                                tint = if (mode == "PIN") B2AccentContent else B2MainText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (mode == "PIN") "Done Selecting" else "Select Home Apps",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = if (mode == "PIN") B2AccentContent else B2MainText
                            )
                        }
                    }

                    TvFocusableSurface(
                        itemId = "all_apps_hero_mode_btn",
                        shape = RoundedCornerShape(12.dp),
                        restingColor = if (mode == "HERO") B2Accent else B2TileSurface,
                        focusedColor = if (mode == "HERO") B2Accent else B2RaisedSurface,
                        onClick = { mode = if (mode == "HERO") "LAUNCH" else "HERO" },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "Set Hero App",
                                tint = if (mode == "HERO") B2AccentContent else B2MainText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (mode == "HERO") "Cancel Hero Pick" else "Set Hero App",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = if (mode == "HERO") B2AccentContent else B2MainText
                            )
                        }
                    }

                    TvFocusableSurface(
                        itemId = "all_apps_close_btn",
                        shape = RoundedCornerShape(12.dp),
                        restingColor = B2TileSurface,
                        focusedColor = B2RaisedSurface,
                        focusRequester = closeFocusRequester,
                        onClick = onClose,
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
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = B2MainText
                            )
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 152.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = installedApps,
                    key = { it.packageName }
                ) { app ->
                    DynamicInstalledAppTile(
                        app = app,
                        showPinBadge = true,
                        onClick = {
                            when (mode) {
                                "PIN" -> onTogglePinApp(app)
                                "HERO" -> {
                                    onSetHeroApp(app)
                                    mode = "LAUNCH"
                                }
                                else -> onAppClick(app)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog when the user clicks "Change app" on the Hero Tile or when Movie Box Pro is not yet selected.
 */
@Composable
fun HeroAppSelectorOrMissingModal(
    currentHeroPackage: String,
    installedApps: List<InstalledTvApp>,
    onSelectHeroApp: (InstalledTvApp) -> Unit,
    onDismiss: () -> Unit
) {
    val closeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { closeFocusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(660.dp)
                .height(460.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(B2QuickSettingsSurface)
                .padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Select Featured Hero Tile App",
                        style = MaterialTheme.typography.headlineMedium,
                        color = B2MainText
                    )
                    Text(
                        text = "Choose Movie Box Pro or any installed APK on your TV to feature on the main Hero Tile",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = B2QuietText
                    )
                }

                TvFocusableSurface(
                    itemId = "hero_selector_close",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    focusRequester = closeFocusRequester,
                    onClick = onDismiss,
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.bodyMedium,
                            color = B2MainText
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 148.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = installedApps,
                    key = { "hero_pick_${it.packageName}" }
                ) { app ->
                    DynamicInstalledAppTile(
                        app = app,
                        onClick = { onSelectHeroApp(app) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                    )
                }
            }
        }
    }
}

/**
 * NVIDIA Shield / Google TV Style Watch Shelf Customizer Modal:
 * Allows adding a show/movie or linking any installed TV APK (like Movie Box Pro, SmartTube, VLC)
 * directly to the Continue (Last Played), Next episode, or To Watch (Favorites) shelf.
 */
@Composable
fun AddToWatchModal(
    activeTab: WatchTab,
    installedApps: List<InstalledTvApp>,
    onDismiss: () -> Unit,
    onAdd: (WatchTab, String, String, String?) -> Unit
) {
    var selectedTab by remember(activeTab) { mutableStateOf(activeTab) }
    var titleInput by remember { mutableStateOf("") }
    var episodeOrNoteInput by remember { mutableStateOf("S1 E1 · Ready to watch") }
    var selectedApp by remember(installedApps) {
        mutableStateOf(
            installedApps.firstOrNull {
                it.packageName.contains("moviebox", ignoreCase = true) ||
                    it.name.contains("Movie Box", ignoreCase = true)
            } ?: installedApps.firstOrNull()
        )
    }
    val addFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { addFocusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(580.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(B2QuickSettingsSurface)
                .padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Customize Home Shelf (${selectedTab.title})",
                style = MaterialTheme.typography.headlineMedium,
                color = B2MainText
            )

            // Shelf Tab Selector (Continue, Next episode, To Watch)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WatchTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    TvFocusableSurface(
                        itemId = "modal_tab_${tab.id}",
                        shape = RoundedCornerShape(14.dp),
                        restingColor = if (isSelected) B2Accent else B2TileSurface,
                        focusedColor = if (isSelected) B2Accent else B2RaisedSurface,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = if (isSelected) B2AccentContent else B2MainText
                            )
                        }
                    }
                }
            }

            Text(
                text = "1. Choose Installed App to Launch (uses its TV thumbnail):",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = B2QuietText
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = installedApps.take(12),
                    key = { "shelf_app_${it.packageName}" }
                ) { app ->
                    val isChosen = selectedApp?.packageName == app.packageName
                    TvFocusableSurface(
                        itemId = "shelf_pick_${app.packageName}",
                        shape = RoundedCornerShape(12.dp),
                        restingColor = if (isChosen) B2Accent else B2TileSurface,
                        focusedColor = if (isChosen) B2Accent else B2RaisedSurface,
                        onClick = {
                            selectedApp = app
                            if (titleInput.isBlank()) {
                                titleInput = app.name
                            }
                        },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = if (isChosen) B2AccentContent else B2MainText
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                singleLine = true,
                label = { Text("Title (or leave blank to use App Name)", color = B2QuietText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = B2Accent,
                    unfocusedBorderColor = B2QuietText,
                    focusedTextColor = B2MainText,
                    unfocusedTextColor = B2MainText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = episodeOrNoteInput,
                onValueChange = { episodeOrNoteInput = it },
                singleLine = true,
                label = { Text("Episode / Subtitle (e.g., S2 E4 next, Favorite, Last played)", color = B2QuietText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = B2Accent,
                    unfocusedBorderColor = B2QuietText,
                    focusedTextColor = B2MainText,
                    unfocusedTextColor = B2MainText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                TvFocusableSurface(
                    itemId = "towatch_modal_cancel",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    onClick = onDismiss,
                    modifier = Modifier.height(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = B2MainText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                TvFocusableSurface(
                    itemId = "towatch_modal_confirm",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2Accent,
                    focusedColor = B2Accent,
                    focusRequester = addFocusRequester,
                    onClick = {
                        val finalTitle = titleInput.ifBlank { selectedApp?.name ?: "Featured Title" }
                        val appLabel = selectedApp?.name ?: "Movie Box Pro"
                        val finalSubtitle = if (episodeOrNoteInput.isNotBlank()) {
                            "$appLabel · $episodeOrNoteInput"
                        } else {
                            appLabel
                        }
                        onAdd(selectedTab, finalTitle, finalSubtitle, selectedApp?.packageName)
                    },
                    modifier = Modifier.height(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save to ${selectedTab.title}", color = B2AccentContent, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/**
 * Full D-Pad Search Overlay for filtering installed TV apps and watchlist items.
 */
@Composable
fun B2SearchOverlayModal(
    installedApps: List<InstalledTvApp>,
    onAppClick: (InstalledTvApp) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(query, installedApps) {
        if (query.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
    }
    val closeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { closeFocusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.86f))
            .padding(horizontal = 56.dp, vertical = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(B2QuickSettingsSurface)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Installed TV Apps",
                    style = MaterialTheme.typography.headlineMedium,
                    color = B2MainText
                )

                TvFocusableSurface(
                    itemId = "search_modal_close",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    focusRequester = closeFocusRequester,
                    onClick = onClose,
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Close", color = B2MainText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("Filter apps by name...", color = B2QuietText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = B2Accent,
                    unfocusedBorderColor = B2QuietText,
                    focusedTextColor = B2MainText,
                    unfocusedTextColor = B2MainText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 152.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = filteredApps,
                    key = { "search_${it.packageName}" }
                ) { app ->
                    DynamicInstalledAppTile(
                        app = app,
                        onClick = { onAppClick(app) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                    )
                }
            }
        }
    }
}
