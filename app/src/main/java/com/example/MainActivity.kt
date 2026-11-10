package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.media.tv.TvInputManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TvSystemRepository
import com.example.model.RailDestination
import com.example.ui.components.AddToWatchModal
import com.example.ui.components.B2AllAppsOverlayModal
import com.example.ui.components.B2DynamicAppsRowSection
import com.example.ui.components.B2HeaderBar
import com.example.ui.components.B2HeroAndWatchShelf
import com.example.ui.components.B2InputsRowSection
import com.example.ui.components.B2QuickSettingsOverlay
import com.example.ui.components.B2SearchOverlayModal
import com.example.ui.components.B2SideRail
import com.example.ui.components.B2WallpaperBackdrop
import com.example.ui.components.B2WallpaperStudioScreen
import com.example.ui.components.HeroAppSelectorOrMissingModal
import com.example.ui.theme.BigDogLauncherTheme
import com.example.viewmodel.LauncherUiState
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()
    private var tvInputManager: TvInputManager? = null

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null) {
                viewModel.refreshDynamicSystemData(context)
            }
        }
    }

    private val hardwareReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null) {
                viewModel.refreshDynamicSystemData(context)
            }
        }
    }

    private val tvInputCallback = object : TvInputManager.TvInputCallback() {
        override fun onInputStateChanged(inputId: String?, state: Int) {
            viewModel.refreshDynamicSystemData(this@MainActivity)
        }

        override fun onInputAdded(inputId: String?) {
            viewModel.refreshDynamicSystemData(this@MainActivity)
        }

        override fun onInputRemoved(inputId: String?) {
            viewModel.refreshDynamicSystemData(this@MainActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerSystemReceivers()
        viewModel.initialize(this)

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
        viewModel.refreshDynamicSystemData(this)
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(packageChangeReceiver) }
        runCatching { unregisterReceiver(hardwareReceiver) }
        runCatching { tvInputManager?.unregisterCallback(tvInputCallback) }
        super.onDestroy()
    }

    private fun registerSystemReceivers() {
        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        val usbFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageChangeReceiver, pkgFilter, Context.RECEIVER_EXPORTED)
            registerReceiver(hardwareReceiver, usbFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(packageChangeReceiver, pkgFilter)
            registerReceiver(hardwareReceiver, usbFilter)
        }

        runCatching {
            tvInputManager = getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
            tvInputManager?.registerCallback(tvInputCallback, Handler(Looper.getMainLooper()))
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_MENU) {
            viewModel.setQuickSettingsOpen(!viewModel.uiState.value.isQuickSettingsOpen)
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun BigDogLauncherScreen(
    viewModel: LauncherViewModel,
    uiState: LauncherUiState
) {
    val context = LocalContext.current
    val heroFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onCustomWallpaperUriPicked(context, uri)
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.onCustomWallpaperUriPicked(context, uri)
    }

    LaunchedEffect(Unit) {
        delay(120L)
        runCatching { heroFocusRequester.requestFocus() }
    }

    BackHandler(
        enabled = uiState.isQuickSettingsOpen ||
            uiState.isHeroSelectorOpen ||
            uiState.isAddToWatchDialogOpen ||
            uiState.isAllAppsOverlayOpen ||
            uiState.isSearchOverlayOpen ||
            uiState.selectedRail != RailDestination.HOME
    ) {
        when {
            uiState.isHeroSelectorOpen -> viewModel.setHeroSelectorOpen(false)
            uiState.isAddToWatchDialogOpen -> viewModel.setAddToWatchDialogOpen(false)
            uiState.isAllAppsOverlayOpen -> viewModel.setAllAppsOverlayOpen(false)
            uiState.isSearchOverlayOpen -> viewModel.setSearchOverlayOpen(false)
            uiState.isQuickSettingsOpen -> viewModel.setQuickSettingsOpen(false)
            uiState.selectedRail != RailDestination.HOME -> {
                viewModel.onSideRailClicked(context, RailDestination.HOME)
            }
        }
    }

    val resolvedHeroApp = TvSystemRepository.resolveHeroApp(
        installedApps = uiState.installedApps,
        preferredPackage = uiState.heroPackageName
    )

    val heroTitle = resolvedHeroApp?.name
        ?: uiState.heroCustomLabel
        ?: "Movie Box Pro"

    val heroSubtitle = if (resolvedHeroApp != null) {
        "Installed on TV · Press OK to open"
    } else {
        "Open your library · Press 'Change app' to select any TV APK"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Active B2 Wallpaper Backdrop (Still, Animated Drift, or User Image + Dim & Blur)
        B2WallpaperBackdrop(
            settings = uiState.wallpaperSettings,
            customBitmap = uiState.customWallpaperBitmap
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // Left 88dp Side Rail (#0B0D0F)
            B2SideRail(
                selectedDestination = uiState.selectedRail,
                onSelectDestination = { destination ->
                    viewModel.onSideRailClicked(context, destination)
                }
            )

            if (uiState.selectedRail == RailDestination.WALLPAPER) {
                B2WallpaperStudioScreen(
                    currentSettings = uiState.wallpaperSettings,
                    customBitmap = uiState.customWallpaperBitmap,
                    onApplyWallpaper = { newSettings ->
                        viewModel.applyWallpaperSettings(context, newSettings)
                    },
                    onPickCustomImage = {
                        val launchedPicker = runCatching {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                            true
                        }.getOrDefault(false)
                        if (!launchedPicker) {
                            runCatching {
                                openDocumentLauncher.launch(arrayOf("image/*"))
                            }
                        }
                    },
                    onBackToHome = {
                        viewModel.onSideRailClicked(context, RailDestination.HOME)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                // Main B2 Home Stage
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 36.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    B2HeaderBar(
                        wifiDisplayName = uiState.wifiDisplayName,
                        onOpenSettingAction = { action, label ->
                            viewModel.launchSettingsWithFallback(context, action, label)
                        },
                        onOpenQuickSettings = { viewModel.setQuickSettingsOpen(true) }
                    )

                    B2HeroAndWatchShelf(
                        heroApp = resolvedHeroApp,
                        heroDisplayTitle = heroTitle,
                        heroDisplaySubtitle = heroSubtitle,
                        selectedTab = uiState.selectedWatchTab,
                        watchItems = uiState.watchItems,
                        heroFocusRequester = heroFocusRequester,
                        onSelectTab = { viewModel.selectWatchTab(it) },
                        onHeroClick = { viewModel.onHeroTileClicked(context) },
                        onChangeHeroAppClick = { viewModel.setHeroSelectorOpen(true) },
                        onWatchItemClick = { item ->
                            viewModel.onWatchItemClicked(context, item)
                        },
                        onOpenCustomizeWatchShelf = { viewModel.setAddToWatchDialogOpen(true) }
                    )

                    B2DynamicAppsRowSection(
                        installedApps = uiState.installedApps,
                        isLoading = uiState.isLoadingApps,
                        onAppClick = { app -> viewModel.onInstalledAppClicked(context, app) },
                        onAllAppsClick = { viewModel.setAllAppsOverlayOpen(true) }
                    )

                    B2InputsRowSection(
                        inputs = uiState.inputsRow,
                        onInputClick = { input -> viewModel.onHardwareInputPillClicked(context, input) }
                    )
                }
            }
        }

        // Slide-over 460dp Quick Settings Panel (#15181B) with scrollable non-squashed 50dp rows
        B2QuickSettingsOverlay(
            isOpen = uiState.isQuickSettingsOpen,
            tiles = uiState.quickSettingTiles,
            rows = uiState.quickSettingRows,
            onClose = { viewModel.setQuickSettingsOpen(false) },
            onSelectAction = { action, label ->
                viewModel.launchSettingsWithFallback(context, action, label)
            },
            onOpenWallpaperStudio = {
                viewModel.setQuickSettingsOpen(false)
                viewModel.onSideRailClicked(context, RailDestination.WALLPAPER)
            }
        )

        // All Apps Full Drawer: Shows all sideloaded APKs & built-in TV apps + Select Home Apps & Set Hero App
        if (uiState.isAllAppsOverlayOpen) {
            B2AllAppsOverlayModal(
                installedApps = uiState.installedApps,
                onAppClick = { app -> viewModel.onInstalledAppClicked(context, app) },
                onTogglePinApp = { app -> viewModel.togglePinInstalledApp(context, app) },
                onSetHeroApp = { app -> viewModel.setFeaturedHeroApp(context, app) },
                onClose = { viewModel.setAllAppsOverlayOpen(false) }
            )
        }

        // Search Installed TV Apps Overlay
        if (uiState.isSearchOverlayOpen) {
            B2SearchOverlayModal(
                installedApps = uiState.installedApps,
                onAppClick = { app -> viewModel.onInstalledAppClicked(context, app) },
                onClose = { viewModel.setSearchOverlayOpen(false) }
            )
        }

        // Hero App Selector Modal
        if (uiState.isHeroSelectorOpen) {
            HeroAppSelectorOrMissingModal(
                currentHeroPackage = uiState.heroPackageName,
                installedApps = uiState.installedApps,
                onSelectHeroApp = { app -> viewModel.setFeaturedHeroApp(context, app) },
                onDismiss = { viewModel.setHeroSelectorOpen(false) }
            )
        }

        // NVIDIA Shield style Watch Shelf Customizer (Continue / Next episode / To Watch)
        if (uiState.isAddToWatchDialogOpen) {
            AddToWatchModal(
                activeTab = uiState.selectedWatchTab,
                installedApps = uiState.installedApps,
                onDismiss = { viewModel.setAddToWatchDialogOpen(false) },
                onAdd = { tab, title, subtitle, targetPkg ->
                    viewModel.addCustomShelfItem(context, tab, title, subtitle, targetPkg)
                }
            )
        }
    }
}
