package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.B2SampleCatalog
import com.example.model.WallpaperPreset
import com.example.model.WallpaperSettings
import com.example.model.WatchItem
import com.example.model.WatchTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bigdog_b2_preferences"
)

data class PersistedLauncherPrefs(
    val showLeftSidebar: Boolean,
    val pinnedPackages: List<String>,
    val hasSavedHomeSelection: Boolean,
    val recentPackages: List<String>,
    val heroPackageName: String,
    val heroCustomLabel: String?,
    val customWatchItems: List<WatchItem>,
    val wallpaperSettings: WallpaperSettings
)

object LauncherPreferencesRepository {
    private val KEY_SHOW_LEFT_SIDEBAR = booleanPreferencesKey("show_left_sidebar")

    private val KEY_PINNED_PACKAGES = stringPreferencesKey("pinned_packages_csv")
    private val KEY_RECENT_PACKAGES = stringPreferencesKey("recent_packages_csv")
    private val KEY_HERO_PACKAGE = stringPreferencesKey("hero_package_name")
    private val KEY_HERO_LABEL = stringPreferencesKey("hero_custom_label")
    private val KEY_CUSTOM_WATCH_ITEMS = stringPreferencesKey("custom_watch_items_v2")
    private val KEY_WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset_id")
    private val KEY_WALLPAPER_URI = stringPreferencesKey("wallpaper_custom_uri")
    private val KEY_WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim_fraction")
    private val KEY_WALLPAPER_BLUR = intPreferencesKey("wallpaper_blur_level")

    fun observePreferences(context: Context): Flow<PersistedLauncherPrefs> {
        return context.applicationContext.launcherDataStore.data.map { prefs ->
            val rawPinned = prefs[KEY_PINNED_PACKAGES] ?: ""
            val pinnedList = rawPinned.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val rawRecent = prefs[KEY_RECENT_PACKAGES] ?: ""
            val recentList = rawRecent.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val heroPkg = prefs[KEY_HERO_PACKAGE] ?: B2SampleCatalog.MOVIE_BOX_PRO_PACKAGE
            val heroLabel = prefs[KEY_HERO_LABEL]

            val rawWatch = prefs[KEY_CUSTOM_WATCH_ITEMS]
            val customWatchList = if (rawWatch.isNullOrBlank()) {
                emptyList()
            } else {
                deserializeWatchItems(rawWatch)
            }

            val presetId = prefs[KEY_WALLPAPER_PRESET] ?: WallpaperPreset.CHARCOAL.id
            val preset = WallpaperPreset.entries.firstOrNull { it.id == presetId }
                ?: WallpaperPreset.CHARCOAL
            val customUri = prefs[KEY_WALLPAPER_URI]?.takeIf { it.isNotBlank() }
            val dim = (prefs[KEY_WALLPAPER_DIM] ?: 0.25f).coerceIn(0f, 0.85f)
            val blur = (prefs[KEY_WALLPAPER_BLUR] ?: 0).coerceIn(0, 3)

            PersistedLauncherPrefs(
                showLeftSidebar = prefs[KEY_SHOW_LEFT_SIDEBAR] ?: true,
                pinnedPackages = pinnedList,
                hasSavedHomeSelection = prefs[KEY_PINNED_PACKAGES] != null,
                recentPackages = recentList,
                heroPackageName = heroPkg,
                heroCustomLabel = heroLabel,
                customWatchItems = customWatchList,
                wallpaperSettings = WallpaperSettings(
                    preset = preset,
                    customImageUri = customUri,
                    dimFraction = dim,
                    blurLevel = blur
                )
            )
        }
    }

    suspend fun saveShowLeftSidebar(context: Context, show: Boolean) {
        context.applicationContext.launcherDataStore.edit { prefs ->
            prefs[KEY_SHOW_LEFT_SIDEBAR] = show
        }
    }

    suspend fun savePinnedPackages(context: Context, packages: List<String>) {
        context.applicationContext.launcherDataStore.edit { prefs ->
            prefs[KEY_PINNED_PACKAGES] = packages.distinct().joinToString(",")
        }
    }

    suspend fun recordRecentAppLaunch(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        context.applicationContext.launcherDataStore.edit { prefs ->
            val current = (prefs[KEY_RECENT_PACKAGES] ?: "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != packageName }
            val updated = (listOf(packageName) + current).take(12)
            prefs[KEY_RECENT_PACKAGES] = updated.joinToString(",")
        }
    }

    suspend fun saveHeroApp(context: Context, packageName: String, label: String?) {
        context.applicationContext.launcherDataStore.edit { prefs ->
            prefs[KEY_HERO_PACKAGE] = packageName
            if (label != null) {
                prefs[KEY_HERO_LABEL] = label
            } else {
                prefs.remove(KEY_HERO_LABEL)
            }
        }
    }

    suspend fun saveCustomWatchItems(context: Context, items: List<WatchItem>) {
        context.applicationContext.launcherDataStore.edit { prefs ->
            prefs[KEY_CUSTOM_WATCH_ITEMS] = serializeWatchItems(items)
        }
    }

    suspend fun saveWallpaperSettings(context: Context, settings: WallpaperSettings) {
        context.applicationContext.launcherDataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_PRESET] = settings.preset.id
            if (settings.customImageUri != null) {
                prefs[KEY_WALLPAPER_URI] = settings.customImageUri
            } else {
                prefs.remove(KEY_WALLPAPER_URI)
            }
            prefs[KEY_WALLPAPER_DIM] = settings.dimFraction
            prefs[KEY_WALLPAPER_BLUR] = settings.blurLevel
        }
    }

    private fun serializeWatchItems(items: List<WatchItem>): String {
        return items.joinToString("\n") { item ->
            val safeTitle = item.title.replace("|", " ").replace("\n", " ")
            val safeSub = item.subtitle.replace("|", " ").replace("\n", " ")
            val safePkg = (item.targetPackage ?: "").replace("|", "")
            "${item.id}|${item.tab.name}|$safeTitle|$safeSub|${item.progress}|$safePkg"
        }
    }

    private fun deserializeWatchItems(serialized: String): List<WatchItem> {
        val lines = serialized.lines().filter { it.isNotBlank() }
        return lines.mapIndexedNotNull { idx, line ->
            val parts = line.split("|")
            if (parts.size < 5) return@mapIndexedNotNull null
            val tab = runCatching { WatchTab.valueOf(parts[1]) }.getOrDefault(WatchTab.TO_WATCH)
            val progress = parts[4].toFloatOrNull() ?: 0f
            val gradient = B2SampleCatalog.cardGradients[idx % B2SampleCatalog.cardGradients.size]
            WatchItem(
                id = parts[0],
                title = parts[2],
                subtitle = parts[3],
                progress = progress,
                gradientStart = gradient.first,
                gradientEnd = gradient.second,
                tab = tab,
                targetPackage = parts.getOrNull(5)?.takeIf { it.isNotBlank() },
                isManualEntry = true
            )
        }
    }
}
