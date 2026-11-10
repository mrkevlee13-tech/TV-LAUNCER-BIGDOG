package com.example

import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.RailDestination
import com.example.ui.components.AddToWatchModal
import com.example.ui.components.B2AppsRowSection
import com.example.ui.components.B2HeaderBar
import com.example.ui.components.B2HeroAndWatchShelf
import com.example.ui.components.B2InputsRowSection
import com.example.ui.components.B2QuickSettingsOverlay
import com.example.ui.components.B2SideRail
import com.example.ui.components.MovieBoxProFallbackModal
import com.example.ui.components.XiaomiAdbSetupInspectorModal
import com.example.ui.theme.B2Accent
import com.example.ui.theme.B2Background
import com.example.ui.theme.B2MainText
import com.example.ui.theme.B2QuietText
import com.example.ui.theme.B2TileSurface
import com.example.ui.theme.BigDogLauncherTheme
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.refreshSystemTelemetry(this)

        setContent {
            BigDogLauncherTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                BigDogLauncherScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSystemTelemetry(this)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyLabel = when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
                KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
                KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
                KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> "OK / DPAD_CENTER"
                KeyEvent.KEYCODE_BACK -> "BACK"
                KeyEvent.KEYCODE_MENU -> {
                    viewModel.setQuickSettingsOpen(!viewModel.uiState.value.isQuickSettingsOpen)
                    "MENU (Quick Settings)"
                }
                else -> "KEY_${event.keyCode}"
            }
            viewModel.onKeyPressedTelemetry(keyLabel)
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun BigDogLauncherScreen(
    viewModel: LauncherViewModel,
    uiState: com.example.viewmodel.LauncherUiState
) {
    val context = LocalContext.current
    val heroFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // Request initial focus on the Movie Box Pro Hero Tile on startup
    LaunchedEffect(Unit) {
        delay(120L)
        runCatching { heroFocusRequester.requestFocus() }
    }

    // Auto-clear transient status banner after 4 seconds
    LaunchedEffect(uiState.statusBannerMessage) {
        if (uiState.statusBannerMessage != null) {
            delay(4000L)
            viewModel.clearStatusBanner()
        }
    }

    // Back button closes overlays or returns rail to Home instead of exiting the launcher
    BackHandler(
        enabled = uiState.isQuickSettingsOpen ||
            uiState.isHeroFallbackDialogOpen ||
            uiState.isAddToWatchDialogOpen ||
            uiState.isAdbSetupModalOpen ||
            uiState.selectedRail != RailDestination.HOME
    ) {
        when {
            uiState.isAdbSetupModalOpen -> viewModel.setAdbSetupModalOpen(false)
            uiState.isHeroFallbackDialogOpen -> viewModel.setHeroFallbackDialogOpen(false)
            uiState.isAddToWatchDialogOpen -> viewModel.setAddToWatchDialogOpen(false)
            uiState.isQuickSettingsOpen -> viewModel.setQuickSettingsOpen(false)
            uiState.selectedRail != RailDestination.HOME -> viewModel.selectRailDestination(RailDestination.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(B2Background)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left 88dp Side Rail (#0B0D0F)
            B2SideRail(
                selectedDestination = uiState.selectedRail,
                onSelectDestination = { viewModel.selectRailDestination(it) },
                onItemFocused = { id, label -> viewModel.onItemFocused(id, label) },
                onOpenBrandInspector = { viewModel.setAdbSetupModalOpen(true) }
            )

            // Main Stage (#101214)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 36.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Bar: Clock (36sp) + Date (15sp) on left; Wi-Fi pill + Bluetooth/Cast/Quick Settings on right
                B2HeaderBar(
                    wifiDisplayName = uiState.wifiDisplayName,
                    isAccessibilityEnabled = uiState.isAccessibilityServiceEnabled,
                    onItemFocused = { id, label -> viewModel.onItemFocused(id, label) },
                    onOpenSettingAction = { action, label ->
                        viewModel.launchSettingsWithFallback(context, action, label)
                    },
                    onOpenQuickSettings = { viewModel.setQuickSettingsOpen(true) },
                    onOpenAdbInspector = { viewModel.setAdbSetupModalOpen(true) }
                )

                // Hero Tile (440x280dp) + Watch Panel (Continue, Next episode, To Watch + 3x2 16:9 grid)
                B2HeroAndWatchShelf(
                    selectedTab = uiState.selectedWatchTab,
                    watchItems = uiState.watchItems,
                    heroFocusRequester = heroFocusRequester,
                    onSelectTab = { viewModel.selectWatchTab(it) },
                    onHeroClick = { viewModel.launchHeroMovieBoxPro(context) },
                    onWatchItemClick = { item ->
                        viewModel.launchHeroMovieBoxPro(context)
                    },
                    onOpenAddToWatch = { viewModel.setAddToWatchDialogOpen(true) },
                    onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
                )

                // Apps Row: 7 tiles of 144x92dp proportion ending with dashed All apps tile
                B2AppsRowSection(
                    apps = uiState.appsRow,
                    onAppClick = { tile -> viewModel.onAppTileClicked(context, tile) },
                    onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
                )

                // Inputs Row: 6 pills 58dp tall with connected/disconnected status dots
                B2InputsRowSection(
                    inputs = uiState.inputsRow,
                    onInputClick = { input -> viewModel.onInputPillClicked(context, input) },
                    onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
                )

                // Milestone 1 Live D-Pad & Remote Focus Telemetry Bar
                Milestone1TelemetryStrip(
                    focusedLabel = uiState.focusedItemLabel,
                    lastKeyLabel = uiState.lastKeyEventLabel,
                    statusMessage = uiState.statusBannerMessage
                )
            }
        }

        // Slide-over 460dp Quick Settings Panel (#15181B)
        B2QuickSettingsOverlay(
            isOpen = uiState.isQuickSettingsOpen,
            tiles = uiState.quickSettingTiles,
            rows = uiState.quickSettingRows,
            onClose = { viewModel.setQuickSettingsOpen(false) },
            onSelectAction = { action, label ->
                viewModel.launchSettingsWithFallback(context, action, label)
            },
            onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
        )

        // Movie Box Pro Package Fallback Prompt
        if (uiState.isHeroFallbackDialogOpen) {
            MovieBoxProFallbackModal(
                currentPackage = uiState.heroTargetPackage,
                onDismiss = { viewModel.setHeroFallbackDialogOpen(false) },
                onSavePackage = { newPkg -> viewModel.updateHeroTargetPackage(newPkg) },
                onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
            )
        }

        // Manual "To Watch" Entry Dialog
        if (uiState.isAddToWatchDialogOpen) {
            AddToWatchModal(
                onDismiss = { viewModel.setAddToWatchDialogOpen(false) },
                onAdd = { title, source -> viewModel.addManualToWatchItem(title, source) },
                onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
            )
        }

        // Xiaomi ADB & Home Screen Override Inspector Modal
        if (uiState.isAdbSetupModalOpen) {
            XiaomiAdbSetupInspectorModal(
                packageName = context.packageName,
                isAccessibilityEnabled = uiState.isAccessibilityServiceEnabled,
                isDefaultHome = uiState.isDefaultHomeApp,
                focusedLabel = uiState.focusedItemLabel,
                lastKeyLabel = uiState.lastKeyEventLabel,
                onClose = { viewModel.setAdbSetupModalOpen(false) },
                onOpenAccessibilitySettings = {
                    viewModel.launchSettingsWithFallback(
                        context,
                        Settings.ACTION_ACCESSIBILITY_SETTINGS,
                        "Accessibility Settings"
                    )
                },
                onItemFocused = { id, label -> viewModel.onItemFocused(id, label) }
            )
        }
    }
}

@Composable
private fun Milestone1TelemetryStrip(
    focusedLabel: String,
    lastKeyLabel: String,
    statusMessage: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(B2TileSurface.copy(alpha = 0.75f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FOCUS: $focusedLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = B2Accent
            )
            Text(
                text = "REMOTE KEY: $lastKeyLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = B2QuietText
            )
        }

        Text(
            text = statusMessage ?: "Milestone 1 Ready · Use D-Pad Arrows & OK",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (statusMessage != null) B2MainText else B2QuietText
        )
    }
}
