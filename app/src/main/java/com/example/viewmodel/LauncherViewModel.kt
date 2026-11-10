package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherPreferencesRepository
import com.example.data.PersistedLauncherPrefs
import com.example.data.TvSystemRepository
import com.example.model.B2SampleCatalog
import com.example.model.InstalledTvApp
import com.example.model.QuickSettingRowItem
import com.example.model.QuickSettingTileItem
import com.example.model.RailDestination
import com.example.model.TvInputPillItem
import com.example.model.WallpaperSettings
import com.example.model.WatchItem
import com.example.model.WatchTab
import com.example.ui.theme.Thumb01End
import com.example.ui.theme.Thumb01Start
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LauncherUiState(
    val showLeftSidebar: Boolean = true,
    val selectedRail: RailDestination = RailDestination.HOME,
    val selectedWatchTab: WatchTab = WatchTab.CONTINUE,
    val watchItems: List<WatchItem> = emptyList(),
    val installedApps: List<InstalledTvApp> = emptyList(),
    val isLoadingApps: Boolean = true,
    val heroPackageName: String = B2SampleCatalog.MOVIE_BOX_PRO_PACKAGE,
    val heroCustomLabel: String? = null,
    val inputsRow: List<TvInputPillItem> = B2SampleCatalog.baselineInputsRow,
    val wallpaperSettings: WallpaperSettings = WallpaperSettings(),
    val customWallpaperBitmap: ImageBitmap? = null,
    val isQuickSettingsOpen: Boolean = false,
    val isHeroSelectorOpen: Boolean = false,
    val isAddToWatchDialogOpen: Boolean = false,
    val isAllAppsOverlayOpen: Boolean = false,
    val hasSavedHomeSelection: Boolean = false,
    val isSavingHomeApps: Boolean = false,
    val isSearchOverlayOpen: Boolean = false,
    val wifiDisplayName: String = "Wi-Fi",
    val quickSettingTiles: List<QuickSettingTileItem> = emptyList(),
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
            id = "qs_row_wallpaper",
            title = "Wallpaper and appearance",
            settingsAction = "",
            opensWallpaperScreen = true
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

    private var hasStartedPrefsCollection = false

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        if (!hasStartedPrefsCollection) {
            hasStartedPrefsCollection = true
            viewModelScope.launch {
                LauncherPreferencesRepository.observePreferences(appContext).collect { prefs ->
                    reloadSystemDataWithPrefs(appContext, prefs)
                }
            }
        } else {
            refreshDynamicSystemData(appContext)
        }
    }

    fun refreshDynamicSystemData(context: Context) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            val prefs = runCatching {
                LauncherPreferencesRepository.observePreferences(appContext).first()
            }.getOrNull()
            if (prefs != null) {
                reloadSystemDataWithPrefs(appContext, prefs)
            }
        }
    }

    private suspend fun reloadSystemDataWithPrefs(
        appContext: Context,
        prefs: PersistedLauncherPrefs
    ) {
        val result = withContext(Dispatchers.IO) {
            val apps = TvSystemRepository.queryAllInstalledTvApps(
                context = appContext,
                pinnedPackages = prefs.pinnedPackages
            )
            val inputs = TvSystemRepository.queryHardwareInputs(appContext)
            val wifiName = TvSystemRepository.resolveWifiDisplayName(appContext)
            val qsTiles = TvSystemRepository.queryQuickSettingTiles(appContext, wifiName)
            val watchList = TvSystemRepository.buildWatchPanelItems(
                context = appContext,
                installedApps = apps,
                recentPackages = prefs.recentPackages,
                customWatchItems = prefs.customWatchItems
            )
            val customWallBmp = TvSystemRepository.loadCustomWallpaperBitmap(
                context = appContext,
                uriString = prefs.wallpaperSettings.customImageUri
            )
            SystemReloadPayload(
                apps = apps,
                inputs = inputs,
                wifiName = wifiName,
                qsTiles = qsTiles,
                watchList = watchList,
                customWallBmp = customWallBmp
            )
        }

        _uiState.update { state ->
            state.copy(
                showLeftSidebar = prefs.showLeftSidebar,
                hasSavedHomeSelection = prefs.hasSavedHomeSelection,
                installedApps = result.apps,
                isLoadingApps = false,
                heroPackageName = prefs.heroPackageName,
                heroCustomLabel = prefs.heroCustomLabel,
                inputsRow = result.inputs,
                wifiDisplayName = result.wifiName,
                quickSettingTiles = result.qsTiles,
                watchItems = result.watchList,
                wallpaperSettings = prefs.wallpaperSettings,
                customWallpaperBitmap = result.customWallBmp
            )
        }
    }

    private data class SystemReloadPayload(
        val apps: List<InstalledTvApp>,
        val inputs: List<TvInputPillItem>,
        val wifiName: String,
        val qsTiles: List<QuickSettingTileItem>,
        val watchList: List<WatchItem>,
        val customWallBmp: ImageBitmap?
    )

    fun setShowLeftSidebar(context: Context, show: Boolean) {
        viewModelScope.launch {
            runCatching {
                LauncherPreferencesRepository.saveShowLeftSidebar(context.applicationContext, show)
            }.onFailure {
                showToast(context, "Unable to save sidebar preference")
            }
        }
    }

    fun onSideRailClicked(context: Context, destination: RailDestination) {
        when (destination) {
            RailDestination.HOME -> {
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.HOME,
                        isAllAppsOverlayOpen = false,
                        isSearchOverlayOpen = false,
                        isQuickSettingsOpen = false
                    )
                }
            }
            RailDestination.MOVIES -> {
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.MOVIES,
                        isAllAppsOverlayOpen = false,
                        isSearchOverlayOpen = false,
                        isQuickSettingsOpen = false
                    )
                }
                onHeroTileClicked(context)
            }
            RailDestination.ALL_APPS -> {
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.ALL_APPS,
                        isAllAppsOverlayOpen = true,
                        isSearchOverlayOpen = false,
                        isQuickSettingsOpen = false
                    )
                }
            }
            RailDestination.INPUTS -> {
                refreshDynamicSystemData(context)
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.INPUTS,
                        isAllAppsOverlayOpen = false,
                        isSearchOverlayOpen = false,
                        isQuickSettingsOpen = false
                    )
                }
                showToast(context, "Refreshed hardware input states")
            }
            RailDestination.SEARCH -> {
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.SEARCH,
                        isSearchOverlayOpen = true,
                        isAllAppsOverlayOpen = false,
                        isQuickSettingsOpen = false
                    )
                }
            }
            RailDestination.WALLPAPER -> {
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.WALLPAPER,
                        isAllAppsOverlayOpen = false,
                        isSearchOverlayOpen = false,
                        isQuickSettingsOpen = false
                    )
                }
            }
            RailDestination.SETTINGS -> {
                refreshDynamicSystemData(context)
                _uiState.update {
                    it.copy(
                        selectedRail = RailDestination.SETTINGS,
                        isQuickSettingsOpen = true
                    )
                }
            }
        }
    }

    fun onHeroTileClicked(context: Context) {
        val appContext = context.applicationContext
        val resolvedHero = TvSystemRepository.resolveHeroApp(
            installedApps = _uiState.value.installedApps,
            preferredPackage = _uiState.value.heroPackageName
        )
        if (resolvedHero != null) {
            TvSystemRepository.launchInstalledApp(context, resolvedHero)
            viewModelScope.launch {
                LauncherPreferencesRepository.recordRecentAppLaunch(appContext, resolvedHero.packageName)
            }
            return
        }

        if (TvSystemRepository.launchPackageIfInstalled(context, _uiState.value.heroPackageName)) {
            viewModelScope.launch {
                LauncherPreferencesRepository.recordRecentAppLaunch(appContext, _uiState.value.heroPackageName)
            }
            return
        }

        _uiState.update { it.copy(isHeroSelectorOpen = true) }
    }

    fun onWatchItemClicked(context: Context, item: WatchItem) {
        val appContext = context.applicationContext
        if (!item.deepLinkUri.isNullOrBlank()) {
            val deepIntent = runCatching {
                Intent.parseUri(item.deepLinkUri, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }.getOrNull()
            if (deepIntent != null && runCatching { context.startActivity(deepIntent); true }.getOrDefault(false)) {
                item.targetPackage?.let { pkg ->
                    viewModelScope.launch {
                        LauncherPreferencesRepository.recordRecentAppLaunch(appContext, pkg)
                    }
                }
                return
            }
        }
        if (!item.targetPackage.isNullOrBlank()) {
            val matchedApp = _uiState.value.installedApps.firstOrNull { it.packageName == item.targetPackage }
            if (matchedApp != null) {
                onInstalledAppClicked(context, matchedApp)
                return
            }
            if (TvSystemRepository.launchPackageIfInstalled(context, item.targetPackage)) {
                viewModelScope.launch {
                    LauncherPreferencesRepository.recordRecentAppLaunch(appContext, item.targetPackage)
                }
                return
            }
        }
        onHeroTileClicked(context)
    }

    fun onInstalledAppClicked(context: Context, app: InstalledTvApp) {
        val appContext = context.applicationContext
        val result = TvSystemRepository.launchInstalledApp(context, app)
        result.onSuccess {
            viewModelScope.launch {
                LauncherPreferencesRepository.recordRecentAppLaunch(appContext, app.packageName)
            }
            _uiState.update {
                it.copy(
                    isAllAppsOverlayOpen = false,
                    isSearchOverlayOpen = false
                )
            }
        }.onFailure {
            showToast(context, "Unable to launch ${app.name}")
        }
    }

    fun confirmHomeApps(context: Context, packages: List<String>) {
        if (_uiState.value.isSavingHomeApps) return
        _uiState.update { it.copy(isSavingHomeApps = true) }
        viewModelScope.launch {
            runCatching {
                LauncherPreferencesRepository.savePinnedPackages(context.applicationContext, packages)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        installedApps = state.installedApps.map { it.copy(isPinned = it.packageName in packages) },
                        hasSavedHomeSelection = true,
                        isSavingHomeApps = false
                    )
                }
                setAllAppsOverlayOpen(false)
            }.onFailure {
                _uiState.update { it.copy(isSavingHomeApps = false) }
                showToast(context, "Unable to save Home apps. Try Confirm again.")
            }
        }
    }

    fun setFeaturedHeroApp(context: Context, app: InstalledTvApp) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            LauncherPreferencesRepository.saveHeroApp(appContext, app.packageName, app.name)
            _uiState.update {
                it.copy(
                    isHeroSelectorOpen = false,
                    isAllAppsOverlayOpen = false
                )
            }
            showToast(appContext, "${app.name} set as Featured Hero Tile")
        }
    }

    fun applyWallpaperSettings(context: Context, newSettings: WallpaperSettings) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            LauncherPreferencesRepository.saveWallpaperSettings(appContext, newSettings)
            _uiState.update {
                it.copy(selectedRail = RailDestination.HOME)
            }
            showToast(appContext, "Wallpaper updated")
        }
    }

    fun onCustomWallpaperUriPicked(context: Context, uri: Uri?) {
        if (uri == null) return
        val appContext = context.applicationContext
        runCatching {
            appContext.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val updated = _uiState.value.wallpaperSettings.copy(
            customImageUri = uri.toString()
        )
        viewModelScope.launch {
            LauncherPreferencesRepository.saveWallpaperSettings(appContext, updated)
            showToast(appContext, "Custom wallpaper image applied")
        }
    }

    fun onHardwareInputPillClicked(context: Context, input: TvInputPillItem) {
        val feedback = TvSystemRepository.activateHardwareInput(context, input)
        showToast(context, feedback)
    }

    fun selectWatchTab(tab: WatchTab) {
        _uiState.update { it.copy(selectedWatchTab = tab) }
    }

    fun setQuickSettingsOpen(open: Boolean) {
        _uiState.update {
            it.copy(
                isQuickSettingsOpen = open,
                selectedRail = if (!open && it.selectedRail == RailDestination.SETTINGS) {
                    RailDestination.HOME
                } else {
                    it.selectedRail
                }
            )
        }
    }

    fun setAllAppsOverlayOpen(open: Boolean) {
        _uiState.update {
            it.copy(
                isAllAppsOverlayOpen = open,
                selectedRail = if (!open && it.selectedRail == RailDestination.ALL_APPS) {
                    RailDestination.HOME
                } else {
                    it.selectedRail
                }
            )
        }
    }

    fun setSearchOverlayOpen(open: Boolean) {
        _uiState.update {
            it.copy(
                isSearchOverlayOpen = open,
                selectedRail = if (!open && it.selectedRail == RailDestination.SEARCH) {
                    RailDestination.HOME
                } else {
                    it.selectedRail
                }
            )
        }
    }

    fun setHeroSelectorOpen(open: Boolean) {
        _uiState.update { it.copy(isHeroSelectorOpen = open) }
    }

    fun setAddToWatchDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isAddToWatchDialogOpen = open) }
    }

    fun addCustomShelfItem(
        context: Context,
        tab: WatchTab,
        title: String,
        subtitle: String,
        targetPackage: String?
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val appContext = context.applicationContext

        viewModelScope.launch {
            val currentPrefs = LauncherPreferencesRepository.observePreferences(appContext).first()
            val newItem = WatchItem(
                id = "custom_${System.currentTimeMillis()}",
                title = cleanTitle,
                subtitle = subtitle.trim().ifEmpty { "Movie Box Pro" },
                progress = when (tab) {
                    WatchTab.CONTINUE -> 0.55f
                    WatchTab.NEXT_EPISODE -> 0.15f
                    WatchTab.TO_WATCH -> 0f
                },
                gradientStart = Thumb01Start,
                gradientEnd = Thumb01End,
                tab = tab,
                targetPackage = targetPackage,
                isManualEntry = true
            )
            val updatedList = listOf(newItem) + currentPrefs.customWatchItems
            LauncherPreferencesRepository.saveCustomWatchItems(appContext, updatedList)
            _uiState.update {
                it.copy(
                    selectedWatchTab = tab,
                    isAddToWatchDialogOpen = false
                )
            }
            showToast(appContext, "Added \"$cleanTitle\" to ${tab.title}")
        }
    }

    fun launchSettingsWithFallback(context: Context, action: String, label: String) {
        if (action == Settings.ACTION_CAST_SETTINGS) {
            showToast(context, TvSystemRepository.activateScreenCast(context))
            return
        }

        val primaryIntent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launchedPrimary = runCatching {
            context.startActivity(primaryIntent)
            true
        }.onFailure { error ->
            Log.w("BigDogSettings", "Unable to open $label: action=$action; trying main Settings", error)
        }.getOrDefault(false)

        if (launchedPrimary) return

        val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launchedFallback = runCatching {
            context.startActivity(fallbackIntent)
            true
        }.onFailure { error ->
            Log.e("BigDogSettings", "Unable to open main Settings after $label: action=$action", error)
        }.getOrDefault(false)

        if (launchedFallback) {
            showToast(context, "$label shortcut not found on Xiaomi ROM; opened main Settings")
        } else {
            showToast(context, "Unable to open $label")
        }
    }

    private fun showToast(context: Context, message: String) {
        runCatching {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
