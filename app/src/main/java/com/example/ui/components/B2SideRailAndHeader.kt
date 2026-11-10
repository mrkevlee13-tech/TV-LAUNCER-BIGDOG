package com.example.ui.components

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SettingsInputComponent
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RailDestination
import com.example.ui.theme.B2Accent
import com.example.ui.theme.B2MainText
import com.example.ui.theme.B2QuietText
import com.example.ui.theme.B2RaisedSurface
import com.example.ui.theme.B2SideRail
import com.example.ui.theme.B2TileSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * B2 Side Rail:
 * - 88dp wide on #0B0D0F
 * - Buttons are 52x52dp with a 24dp icon: Home, Movies, All apps, Inputs at the top
 *   and Search, Settings at the bottom.
 * - Active item has a raised background (#1F252B) and a 4dp wide accent bar (#8FD6B0).
 */
@Composable
fun B2SideRail(
    selectedDestination: RailDestination,
    onSelectDestination: (RailDestination) -> Unit,
    onItemFocused: (String, String) -> Unit,
    onOpenBrandInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topItems = listOf(
        RailDestination.HOME,
        RailDestination.MOVIES,
        RailDestination.ALL_APPS,
        RailDestination.INPUTS
    )
    val bottomItems = listOf(
        RailDestination.SEARCH,
        RailDestination.SETTINGS
    )

    Column(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .background(B2SideRail)
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top BigDog Brand Ring (matches top-left green ring in B2 mockup)
            TvFocusableSurface(
                itemId = "rail_brand_logo",
                itemLabel = "BigDog Launcher · ADB & Milestone 1 Inspector",
                shape = CircleShape,
                restingColor = Color.Transparent,
                focusedColor = B2RaisedSurface,
                onFocused = onItemFocused,
                onClick = onOpenBrandInspector,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(2.5.dp, B2Accent, CircleShape)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            topItems.forEach { destination ->
                SideRailButton(
                    destination = destination,
                    isSelected = selectedDestination == destination,
                    icon = railIconFor(destination, isSelected = selectedDestination == destination),
                    onFocused = onItemFocused,
                    onClick = { onSelectDestination(destination) }
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            bottomItems.forEach { destination ->
                SideRailButton(
                    destination = destination,
                    isSelected = selectedDestination == destination,
                    icon = railIconFor(destination, isSelected = selectedDestination == destination),
                    onFocused = onItemFocused,
                    onClick = { onSelectDestination(destination) }
                )
            }
        }
    }
}

@Composable
private fun SideRailButton(
    destination: RailDestination,
    isSelected: Boolean,
    icon: ImageVector,
    onFocused: (String, String) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        // 4dp wide accent bar on the left edge when active
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(B2Accent)
            )
        }

        TvFocusableSurface(
            itemId = destination.id,
            itemLabel = "Side Rail · ${destination.label}",
            shape = RoundedCornerShape(16.dp),
            restingColor = if (isSelected) B2RaisedSurface else Color.Transparent,
            focusedColor = B2RaisedSurface,
            onFocused = onFocused,
            onClick = onClick,
            modifier = Modifier.size(52.dp)
        ) { isFocused ->
            Icon(
                imageVector = icon,
                contentDescription = destination.label,
                tint = if (isSelected || isFocused) B2MainText else B2QuietText,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

private fun railIconFor(destination: RailDestination, isSelected: Boolean): ImageVector {
    return when (destination) {
        RailDestination.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        RailDestination.MOVIES -> Icons.Outlined.SmartDisplay
        RailDestination.ALL_APPS -> Icons.Outlined.Apps
        RailDestination.INPUTS -> Icons.Outlined.SettingsInputComponent
        RailDestination.SEARCH -> Icons.Outlined.Search
        RailDestination.SETTINGS -> Icons.Outlined.Tune
    }
}

/**
 * B2 Header Bar:
 * - Left: Clock at 36sp (Space Grotesk) and date at 15sp in quiet text (#9AA3AD).
 * - Right: Wi-Fi pill 44dp tall showing network name, then round 44dp buttons for
 *   Bluetooth, Cast, and Quick settings (plus an ADB/D-Pad status button for Milestone 1 testing).
 */
@Composable
fun B2HeaderBar(
    wifiDisplayName: String,
    isAccessibilityEnabled: Boolean,
    onItemFocused: (String, String) -> Unit,
    onOpenSettingAction: (String, String) -> Unit,
    onOpenQuickSettings: () -> Unit,
    onOpenAdbInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeText by remember { mutableStateOf("19:30") }
    var currentDateText by remember { mutableStateOf("Tuesday 10 November") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeText = timeFormat.format(now)
            currentDateText = dateFormat.format(now)
            delay(15_000L)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Clock (36sp Space Grotesk) + Date (15sp Inter in quiet text)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = currentTimeText,
                style = MaterialTheme.typography.displayLarge,
                color = B2MainText
            )
            Text(
                text = currentDateText,
                style = MaterialTheme.typography.titleMedium,
                color = B2QuietText,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }

        // Right: Wi-Fi pill (44dp tall) + round 44dp buttons for Bluetooth, Cast, Quick Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Milestone 1 / Xiaomi Home Override Diagnostic Pill
            TvFocusableSurface(
                itemId = "header_adb_setup_pill",
                itemLabel = "Xiaomi Home Override & D-Pad Diagnostic",
                shape = RoundedCornerShape(22.dp),
                restingColor = B2TileSurface,
                focusedColor = B2RaisedSurface,
                onFocused = onItemFocused,
                onClick = onOpenAdbInspector,
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Terminal,
                        contentDescription = "ADB Setup & D-Pad Status",
                        tint = B2Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isAccessibilityEnabled) "Home Override: ON" else "ADB Setup",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = if (isAccessibilityEnabled) B2Accent else B2QuietText
                    )
                }
            }

            // Wi-Fi Pill (44dp tall)
            TvFocusableSurface(
                itemId = "header_wifi_pill",
                itemLabel = "Header · Wi-Fi ($wifiDisplayName)",
                shape = RoundedCornerShape(22.dp),
                restingColor = B2TileSurface,
                focusedColor = B2RaisedSurface,
                onFocused = onItemFocused,
                onClick = { onOpenSettingAction(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings") },
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = B2Accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = wifiDisplayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = B2MainText
                    )
                }
            }

            // Round 44dp Bluetooth Button
            HeaderCircleButton(
                id = "header_bluetooth_button",
                label = "Header · Bluetooth",
                icon = Icons.Outlined.Bluetooth,
                onFocused = onItemFocused,
                onClick = {
                    onOpenSettingAction(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
                }
            )

            // Round 44dp Cast Button
            HeaderCircleButton(
                id = "header_cast_button",
                label = "Header · Cast",
                icon = Icons.Outlined.Cast,
                onFocused = onItemFocused,
                onClick = {
                    onOpenSettingAction(Settings.ACTION_CAST_SETTINGS, "Cast Settings")
                }
            )

            // Round 44dp Quick Settings Button
            HeaderCircleButton(
                id = "header_quick_settings_button",
                label = "Header · Quick settings",
                icon = Icons.Outlined.Tune,
                onFocused = onItemFocused,
                onClick = onOpenQuickSettings
            )
        }
    }
}

@Composable
private fun HeaderCircleButton(
    id: String,
    label: String,
    icon: ImageVector,
    onFocused: (String, String) -> Unit,
    onClick: () -> Unit
) {
    TvFocusableSurface(
        itemId = id,
        itemLabel = label,
        shape = CircleShape,
        restingColor = B2TileSurface,
        focusedColor = B2RaisedSurface,
        onFocused = onFocused,
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = B2MainText,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center)
        )
    }
}
