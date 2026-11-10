package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.example.model.AppTileItem
import com.example.model.B2SampleCatalog
import com.example.model.QuickSettingRowItem
import com.example.model.QuickSettingTileItem
import com.example.model.RailDestination
import com.example.model.TvInputPillItem
import com.example.model.WatchItem
import com.example.model.WatchTab
import com.example.service.BigDogHomeAccessibilityService
import com.example.ui.theme.Thumb01End
import com.example.ui.theme.Thumb01Start
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LauncherUiState(
    val selectedRail: RailDestination = RailDestination.HOME,
    val selectedWatchTab: WatchTab = WatchTab.CONTINUE,
    val watchItems: List<WatchItem> = B2SampleCatalog.initialWatchItems,
    val appsRow: List<AppTileItem> = B2SampleCatalog.defaultAppsRow,
    val inputsRow: List<TvInputPillItem> = B2SampleCatalog.defaultInputsRow,
    val heroTargetPackage: String = B2SampleCatalog.DEFAULT_MOVIE_BOX_PACKAGE,
    val focusedItemId: String = "hero_movie_box_pro",
    val focusedItemLabel: String = "Movie Box Pro (Hero Tile)",
    val lastKeyEventLabel: String = "Ready for D-Pad navigation",
    val statusBannerMessage: String? = null,
    val isQuickSettingsOpen: Boolean = false,
    val isHeroFallbackDialogOpen: Boolean = false,
    val isAddToWatchDialogOpen: Boolean = false,
    val isAdbSetupModalOpen: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val isDefaultHomeApp: Boolean = false,
    val wifiDisplayName: String = "Home 5G",
    val quickSettingTiles: List<QuickSettingTileItem> = listOf(
        QuickSettingTileItem(
            id = "qs_tile_wifi",
            title = "Wi-Fi",
            subtitle = "Home 5G · Strong",
            isActive = true,
            settingsAction = Settings.ACTION_WIFI_SETTINGS
        ),
        QuickSettingTileItem(
            id = "qs_tile_bt",
            title = "Bluetooth",
            subtitle = "On · 1 device",
            isActive = true,
            settingsAction = Settings.ACTION_BLUETOOTH_SETTINGS
        ),
        QuickSettingTileItem(
            id = "qs_tile_cast",
            title = "Cast",
            subtitle = "Ready",
            isActive = false,
            settingsAction = Settings.ACTION_CAST_SETTINGS
        ),
        QuickSettingTileItem(
            id = "qs_tile_usb",
            title = "USB",
            subtitle = "Drive connected",
            isActive = true,
            settingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
        )
    ),
    val quickSettingRows: List<QuickSettingRowItem> = listOf(
        QuickSettingRowItem(
            id = "qs_row_display",
            title = "Display and picture",
            settingsAction = Settings.ACTION_DISPLAY_SETTINGS
        ),
        QuickSettingRowItem(
            id = "qs_row_sound",
            title = "Sound",
            settingsAction = Settings.ACTION_SOUND_SETTINGS
        ),
        QuickSettingRowItem(
            id = "qs_row_network",
            title = "Network and internet",
            settingsAction = Settings.ACTION_WIRELESS_SETTINGS
        ),
        QuickSettingRowItem(
            id = "qs_row_storage",
            title = "Storage",
            settingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
        ),
        QuickSettingRowItem(
            id = "qs_row_date",
            title = "Date and time",
            settingsAction = Settings.ACTION_DATE_SETTINGS
        ),
        QuickSettingRowItem(
            id = "qs_row_all_settings",
            title = "All TV settings",
            settingsAction = Settings.ACTION_SETTINGS,
            isHighlightedAccent = true
        )
    )
)

class LauncherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    fun onItemFocused(itemId: String, itemLabel: String) {
        _uiState.update { state ->
            state.copy(
                focusedItemId = itemId,
                focusedItemLabel = itemLabel
            )
        }
    }

    fun onKeyPressedTelemetry(keyName: String) {
        _uiState.update { state ->
            state.copy(lastKeyEventLabel = keyName)
        }
    }

    fun selectRailDestination(destination: RailDestination) {
        _uiState.update { state ->
            when (destination) {
                RailDestination.SETTINGS -> state.copy(
                    selectedRail = destination,
                    isQuickSettingsOpen = true,
                    statusBannerMessage = "Opened Quick Settings panel"
                )
                else -> state.copy(
                    selectedRail = destination,
                    statusBannerMessage = "Switched section to ${destination.label}"
                )
            }
        }
    }

    fun selectWatchTab(tab: WatchTab) {
        _uiState.update { state ->
            state.copy(
                selectedWatchTab = tab,
                statusBannerMessage = "Watch panel: ${tab.title}"
            )
        }
    }

    fun setQuickSettingsOpen(open: Boolean) {
        _uiState.update { state ->
            state.copy(isQuickSettingsOpen = open)
        }
    }

    fun setAdbSetupModalOpen(open: Boolean) {
        _uiState.update { state ->
            state.copy(isAdbSetupModalOpen = open)
        }
    }

    fun setHeroFallbackDialogOpen(open: Boolean) {
        _uiState.update { state ->
            state.copy(isHeroFallbackDialogOpen = open)
        }
    }

    fun setAddToWatchDialogOpen(open: Boolean) {
        _uiState.update { state ->
            state.copy(isAddToWatchDialogOpen = open)
        }
    }

    fun addManualToWatchItem(title: String, sourceApp: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val cleanSource = sourceApp.trim().ifEmpty { "Movie Box Pro" }
        val newItem = WatchItem(
            id = "towatch_${System.currentTimeMillis()}",
            title = cleanTitle,
            subtitle = "$cleanSource · Manual To Watch",
            progress = 0f,
            gradientStart = Thumb01Start,
            gradientEnd = Thumb01End,
            tab = WatchTab.TO_WATCH
        )
        _uiState.update { state ->
            state.copy(
                watchItems = listOf(newItem) + state.watchItems,
                selectedWatchTab = WatchTab.TO_WATCH,
                isAddToWatchDialogOpen = false,
                statusBannerMessage = "Added \"$cleanTitle\" to To Watch"
            )
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun refreshSystemTelemetry(context: Context) {
        val pkgName = context.packageName
        val enabledServices = runCatching {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
        }.getOrDefault("")

        val isAccEnabled = BigDogHomeAccessibilityService.isServiceRunning ||
            enabledServices.contains(pkgName, ignoreCase = true)

        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolvedHomePkg = runCatching {
            context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
        }.getOrNull()
        val isDefaultHome = resolvedHomePkg == pkgName

        val wifiLabel = resolveWifiDisplayName(context)

        _uiState.update { state ->
            state.copy(
                isAccessibilityServiceEnabled = isAccEnabled,
                isDefaultHomeApp = isDefaultHome,
                wifiDisplayName = wifiLabel
            )
        }
    }

    fun launchHeroMovieBoxPro(context: Context) {
        val targetPackage = _uiState.value.heroTargetPackage
        if (tryLaunchPackage(context, targetPackage)) {
            _uiState.update {
                it.copy(statusBannerMessage = "Launching Movie Box Pro ($targetPackage)...")
            }
        } else {
            _uiState.update {
                it.copy(
                    isHeroFallbackDialogOpen = true,
                    statusBannerMessage = "Movie Box Pro ($targetPackage) is not installed yet"
                )
            }
        }
    }

    fun updateHeroTargetPackage(newPackage: String) {
        val trimmed = newPackage.trim().ifEmpty { B2SampleCatalog.DEFAULT_MOVIE_BOX_PACKAGE }
        _uiState.update {
            it.copy(
                heroTargetPackage = trimmed,
                isHeroFallbackDialogOpen = false,
                statusBannerMessage = "Hero target updated to $trimmed"
            )
        }
    }

    fun onAppTileClicked(context: Context, tile: AppTileItem) {
        if (tile.isAllAppsCard) {
            _uiState.update {
                it.copy(
                    selectedRail = RailDestination.ALL_APPS,
                    statusBannerMessage = "Selected All apps (${it.appsRow.size - 1} pinned tiles)"
                )
            }
            return
        }
        if (tryLaunchPackage(context, tile.packageName)) {
            _uiState.update {
                it.copy(statusBannerMessage = "Launching ${tile.name}...")
            }
        } else {
            _uiState.update {
                it.copy(
                    statusBannerMessage = "${tile.name} (${tile.packageName}) not installed yet · Focus & click verified"
                )
            }
        }
    }

    fun onInputPillClicked(context: Context, input: TvInputPillItem) {
        if (input.inputUriOrAction.startsWith("content://")) {
            val uriIntent = Intent(Intent.ACTION_VIEW, Uri.parse(input.inputUriOrAction)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val started = runCatching {
                context.startActivity(uriIntent)
                true
            }.getOrDefault(false)

            if (!started) {
                _uiState.update {
                    it.copy(
                        statusBannerMessage = "${input.label} (${input.status}) · Ready for Xiaomi TvInputManager passthrough"
                    )
                }
            }
        } else {
            launchSettingsWithFallback(context, input.inputUriOrAction, input.label)
        }
    }

    fun launchSettingsWithFallback(context: Context, action: String, label: String) {
        val primaryIntent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launchedPrimary = runCatching {
            context.startActivity(primaryIntent)
            true
        }.getOrDefault(false)

        if (launchedPrimary) {
            _uiState.update {
                it.copy(statusBannerMessage = "Opened $label")
            }
            return
        }

        val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launchedFallback = runCatching {
            context.startActivity(fallbackIntent)
            true
        }.getOrDefault(false)

        _uiState.update {
            it.copy(
                statusBannerMessage = if (launchedFallback) {
                    "$label shortcut not supported by Xiaomi ROM; opened main Settings"
                } else {
                    "Selected $label ($action)"
                }
            )
        }
    }

    private fun tryLaunchPackage(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val pm = context.packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
            ?: pm.getLaunchIntentForPackage(packageName)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    @Suppress("DEPRECATION")
    private fun resolveWifiDisplayName(context: Context): String {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNet)
            if (caps == null) {
                "Home 5G"
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                "Ethernet"
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val rawSsid = wm?.connectionInfo?.ssid?.trim('"')
                if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>") {
                    rawSsid
                } else {
                    "Home 5G"
                }
            } else {
                "Home 5G"
            }
        }.getOrDefault("Home 5G")
    }
}
