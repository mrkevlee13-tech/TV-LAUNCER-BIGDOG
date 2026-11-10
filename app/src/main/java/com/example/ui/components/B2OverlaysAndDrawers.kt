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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuickSettingRowItem
import com.example.model.QuickSettingTileItem
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
 * - 2x2 grid of 84dp tall tiles for Wi-Fi, Bluetooth, Cast, and USB, each showing its state.
 * - Below are six 50dp tall rows with a chevron: Display and picture, Sound, Network and internet,
 *   Storage, Date and time, and All TV settings.
 */
@Composable
fun B2QuickSettingsOverlay(
    isOpen: Boolean,
    tiles: List<QuickSettingTileItem>,
    rows: List<QuickSettingRowItem>,
    onClose: () -> Unit,
    onSelectAction: (String, String) -> Unit,
    onItemFocused: (String, String) -> Unit
) {
    val firstTileFocusRequester = remember { FocusRequester() }

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
            runCatching { firstTileFocusRequester.requestFocus() }
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
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: "Quick settings" + round close button
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
                        itemLabel = "Quick Settings · Close",
                        shape = CircleShape,
                        restingColor = B2TileSurface,
                        focusedColor = B2RaisedSurface,
                        onFocused = onItemFocused,
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

                // 2x2 grid of 84dp tall tiles for Wi-Fi, Bluetooth, Cast, USB
                val row1 = tiles.take(2)
                val row2 = tiles.drop(2).take(2)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row1.forEachIndexed { index, tile ->
                            QuickSettingTileCard(
                                tile = tile,
                                focusRequester = if (index == 0) firstTileFocusRequester else null,
                                onFocused = onItemFocused,
                                onClick = { onSelectAction(tile.settingsAction, tile.title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
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
                                onFocused = onItemFocused,
                                onClick = { onSelectAction(tile.settingsAction, tile.title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TV SETTINGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = B2QuietText
                )

                // Six 50dp tall rows with a chevron
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        QuickSettingRowCard(
                            row = row,
                            onFocused = onItemFocused,
                            onClick = { onSelectAction(row.settingsAction, row.title) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
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
    onFocused: (String, String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableSurface(
        itemId = tile.id,
        itemLabel = "Quick Setting · ${tile.title} (${tile.subtitle})",
        shape = RoundedCornerShape(16.dp),
        restingColor = B2TileSurface,
        focusedColor = B2RaisedSurface,
        focusRequester = focusRequester,
        onFocused = onFocused,
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
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = B2MainText
                )
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = B2QuietText
                )
            }
        }
    }
}

@Composable
private fun QuickSettingRowCard(
    row: QuickSettingRowItem,
    onFocused: (String, String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (row.isHighlightedAccent) B2Accent else B2MainText
    TvFocusableSurface(
        itemId = row.id,
        itemLabel = "TV Settings · ${row.title}",
        shape = RoundedCornerShape(12.dp),
        restingColor = B2TileSurface,
        focusedColor = B2RaisedSurface,
        focusScale = 1.03f,
        onFocused = onFocused,
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
                    color = textColor
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
        else -> Icons.Outlined.Tune
    }
}

/**
 * Fallback Prompt Modal when Movie Box Pro is not yet installed on the TV.
 */
@Composable
fun MovieBoxProFallbackModal(
    currentPackage: String,
    onDismiss: () -> Unit,
    onSavePackage: (String) -> Unit,
    onItemFocused: (String, String) -> Unit
) {
    var packageInput by remember(currentPackage) { mutableStateOf(currentPackage) }
    val saveFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { saveFocusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(B2QuickSettingsSurface)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Movie Box Pro Package Setup",
                style = MaterialTheme.typography.headlineMedium,
                color = B2MainText
            )
            Text(
                text = "Package '$currentPackage' is not installed on this TV yet. Once you sideload Movie Box Pro, enter its package name below or keep the default.",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = B2QuietText
            )

            OutlinedTextField(
                value = packageInput,
                onValueChange = { packageInput = it },
                singleLine = true,
                label = { Text("Target Package Name", color = B2QuietText) },
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
                    itemId = "hero_modal_cancel",
                    itemLabel = "Cancel Hero Package Edit",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    onFocused = onItemFocused,
                    onClick = onDismiss,
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Close", color = B2MainText, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                TvFocusableSurface(
                    itemId = "hero_modal_save",
                    itemLabel = "Save Hero Package Name",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2Accent,
                    focusedColor = B2Accent,
                    focusRequester = saveFocusRequester,
                    onFocused = onItemFocused,
                    onClick = { onSavePackage(packageInput) },
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save Package", color = B2AccentContent, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/**
 * Manual "To Watch" Item Dialog
 */
@Composable
fun AddToWatchModal(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    onItemFocused: (String, String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var sourceInput by remember { mutableStateOf("Movie Box Pro") }
    val addFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { addFocusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(480.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(B2QuickSettingsSurface)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add to Manual 'To Watch' List",
                style = MaterialTheme.typography.headlineMedium,
                color = B2MainText
            )
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                singleLine = true,
                label = { Text("Movie or Episode Title", color = B2QuietText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = B2Accent,
                    unfocusedBorderColor = B2QuietText,
                    focusedTextColor = B2MainText,
                    unfocusedTextColor = B2MainText
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sourceInput,
                onValueChange = { sourceInput = it },
                singleLine = true,
                label = { Text("Source App (e.g., Movie Box Pro, SmartTube)", color = B2QuietText) },
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
                    itemLabel = "Cancel Add To Watch",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    onFocused = onItemFocused,
                    onClick = onDismiss,
                    modifier = Modifier.height(44.dp)
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
                    itemLabel = "Confirm Add To Watch",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2Accent,
                    focusedColor = B2Accent,
                    focusRequester = addFocusRequester,
                    onFocused = onItemFocused,
                    onClick = {
                        onAdd(
                            titleInput.ifBlank { "New Watchlist Title" },
                            sourceInput
                        )
                    },
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add Title", color = B2AccentContent, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/**
 * Xiaomi ADB Home Screen Override & Milestone 1 D-Pad Inspector Modal
 */
@Composable
fun XiaomiAdbSetupInspectorModal(
    packageName: String,
    isAccessibilityEnabled: Boolean,
    isDefaultHome: Boolean,
    focusedLabel: String,
    lastKeyLabel: String,
    onClose: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onItemFocused: (String, String) -> Unit
) {
    val closeFocusRequester = remember { FocusRequester() }
    val serviceName = "$packageName/com.example.service.BigDogHomeAccessibilityService"

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
                .width(680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(B2QuickSettingsSurface)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "BigDog Launcher (B2) · Milestone 1 & Xiaomi ADB Setup",
                style = MaterialTheme.typography.headlineMedium,
                color = B2MainText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusBadgeBox(
                    title = "Accessibility Service",
                    value = if (isAccessibilityEnabled) "ACTIVE" else "NOT ENABLED YET",
                    isGood = isAccessibilityEnabled,
                    modifier = Modifier.weight(1f)
                )
                StatusBadgeBox(
                    title = "Default Home Role",
                    value = if (isDefaultHome) "DEFAULT HOME" else "STANDARD / OVERRIDE",
                    isGood = isDefaultHome,
                    modifier = Modifier.weight(1f)
                )
                StatusBadgeBox(
                    title = "Last D-Pad Input",
                    value = lastKeyLabel,
                    isGood = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(B2TileSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PowerShell Commands (or run .\\setup_xiaomi_tv.ps1):",
                    style = MaterialTheme.typography.labelMedium,
                    color = B2Accent
                )
                Text(
                    text = "& \$adb connect 192.168.3.16:5555\n" +
                        "& \$adb install -r .\\app\\build\\outputs\\apk\\debug\\app-debug.apk\n" +
                        "& \$adb shell settings put secure enabled_accessibility_services \"$serviceName\"\n" +
                        "& \$adb shell settings put secure accessibility_enabled 1\n" +
                        "& \$adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = B2MainText
                )
            }

            Text(
                text = "Current Focused Element: $focusedLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = B2QuietText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                TvFocusableSurface(
                    itemId = "adb_modal_open_accessibility",
                    itemLabel = "Open System Accessibility Settings",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2TileSurface,
                    focusedColor = B2RaisedSurface,
                    onFocused = onItemFocused,
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open TV Accessibility Settings",
                            color = B2MainText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                TvFocusableSurface(
                    itemId = "adb_modal_close",
                    itemLabel = "Close ADB Inspector",
                    shape = RoundedCornerShape(12.dp),
                    restingColor = B2Accent,
                    focusedColor = B2Accent,
                    focusRequester = closeFocusRequester,
                    onFocused = onItemFocused,
                    onClick = onClose,
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            color = B2AccentContent,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeBox(
    title: String,
    value: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(B2TileSurface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = B2QuietText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isGood) B2Accent else B2MainText
        )
    }
}
