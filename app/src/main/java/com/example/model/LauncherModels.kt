package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.ui.theme.Thumb01End
import com.example.ui.theme.Thumb01Start
import com.example.ui.theme.Thumb02End
import com.example.ui.theme.Thumb02Start
import com.example.ui.theme.Thumb03End
import com.example.ui.theme.Thumb03Start
import com.example.ui.theme.Thumb05End
import com.example.ui.theme.Thumb05Start

enum class RailDestination(val id: String, val label: String) {
    HOME("rail_home", "Home"),
    MOVIES("rail_movies", "Movies"),
    ALL_APPS("rail_all_apps", "All apps"),
    INPUTS("rail_inputs", "Inputs"),
    SEARCH("rail_search", "Search"),
    WALLPAPER("rail_wallpaper", "Wallpaper"),
    SETTINGS("rail_settings", "Settings")
}

enum class WatchTab(val id: String, val title: String) {
    CONTINUE("tab_continue", "Continue"),
    NEXT_EPISODE("tab_next_episode", "Next episode"),
    TO_WATCH("tab_to_watch", "To Watch")
}

/**
 * Represents a thumbnail card in the Watch Panel (Continue, Next episode, To Watch).
 * Can be backed by a real installed TV app (with its actual bannerBitmap/iconBitmap)
 * or a Watch Next / manual To Watch entry.
 */
data class WatchItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val gradientStart: Color,
    val gradientEnd: Color,
    val tab: WatchTab,
    val targetPackage: String? = null,
    val deepLinkUri: String? = null,
    val bannerBitmap: ImageBitmap? = null,
    val iconBitmap: ImageBitmap? = null,
    val isManualEntry: Boolean = false
)

/**
 * Dynamically resolved application installed on the TV via PackageManager.
 */
data class InstalledTvApp(
    val id: String,
    val name: String,
    val packageName: String,
    val activityName: String,
    val isLeanback: Boolean,
    val bannerBitmap: ImageBitmap? = null,
    val iconBitmap: ImageBitmap? = null,
    val isPinned: Boolean = false,
    val isSystemApp: Boolean = false
)

enum class HardwareInputType {
    HDMI_1,
    HDMI_2,
    HDMI_3,
    AV,
    USB,
    SCREEN_CAST
}

data class TvInputPillItem(
    val id: String,
    val label: String,
    val status: String,
    val isConnected: Boolean,
    val inputType: HardwareInputType,
    val tvInputId: String? = null
)

data class QuickSettingTileItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isActive: Boolean,
    val settingsAction: String
)

data class QuickSettingRowItem(
    val id: String,
    val title: String,
    val settingsAction: String,
    val isHighlightedAccent: Boolean = false,
    val opensWallpaperScreen: Boolean = false
)

enum class WallpaperPreset(
    val id: String,
    val title: String,
    val isAnimated: Boolean,
    val colors: List<Color>
) {
    CHARCOAL(
        id = "charcoal",
        title = "Charcoal",
        isAnimated = false,
        colors = listOf(Color(0xFF101214), Color(0xFF14181C), Color(0xFF0C0E10))
    ),
    AURORA(
        id = "aurora",
        title = "Aurora",
        isAnimated = true,
        colors = listOf(Color(0xFF0E2A23), Color(0xFF163D35), Color(0xFF101C2E), Color(0xFF0D1318))
    ),
    DEEP_SPACE(
        id = "deep_space",
        title = "Deep space",
        isAnimated = true,
        colors = listOf(Color(0xFF0C1021), Color(0xFF1B1938), Color(0xFF0E1D2B), Color(0xFF090B10))
    ),
    SLOW_WAVES(
        id = "slow_waves",
        title = "Slow waves",
        isAnimated = true,
        colors = listOf(Color(0xFF0F222D), Color(0xFF1A3644), Color(0xFF122723), Color(0xFF0D1216))
    ),
    SUNSET(
        id = "sunset",
        title = "Sunset",
        isAnimated = false,
        colors = listOf(Color(0xFF2D191E), Color(0xFF3B2219), Color(0xFF141318))
    ),
    FOREST_MIST(
        id = "forest_mist",
        title = "Forest mist",
        isAnimated = false,
        colors = listOf(Color(0xFF11241D), Color(0xFF1B332B), Color(0xFF0E1513))
    )
}

data class WallpaperSettings(
    val preset: WallpaperPreset = WallpaperPreset.CHARCOAL,
    val customImageUri: String? = null,
    val dimFraction: Float = 0.25f,
    val blurLevel: Int = 0, // 0 = Off, 1 = Subtle, 2 = Medium, 3 = High
    val animationEnabled: Boolean = true
)

/** A resumable destination, not fabricated programme metadata or progress. */
data class HeroSource(
    val kind: String, // app, input, media
    val target: String,
    val title: String,
    val mimeType: String? = null
)

object B2SampleCatalog {
    const val MOVIE_BOX_PRO_PACKAGE = "com.movieboxpro.android"

    val defaultManualToWatch: List<WatchItem> = listOf(
        WatchItem(
            id = "towatch_01",
            title = "Add movies or shows to your watchlist",
            subtitle = "Press '+ Add title' above to save items here",
            progress = 0.0f,
            gradientStart = Thumb03Start,
            gradientEnd = Thumb03End,
            tab = WatchTab.TO_WATCH,
            isManualEntry = true
        )
    )

    val cardGradients: List<Pair<Color, Color>> = listOf(
        Thumb01Start to Thumb01End,
        Thumb02Start to Thumb02End,
        Thumb03Start to Thumb03End,
        Thumb05Start to Thumb05End
    )

    /**
     * Honest baseline before TvInputManager / StorageManager query completes:
     * Nothing is falsely reported as connected.
     */
    val baselineInputsRow: List<TvInputPillItem> = listOf(
        TvInputPillItem(
            id = "input_hdmi1",
            label = "HDMI 1",
            status = "No signal",
            isConnected = false,
            inputType = HardwareInputType.HDMI_1
        ),
        TvInputPillItem(
            id = "input_hdmi2",
            label = "HDMI 2",
            status = "No signal",
            isConnected = false,
            inputType = HardwareInputType.HDMI_2
        ),
        TvInputPillItem(
            id = "input_hdmi3",
            label = "HDMI 3",
            status = "No signal",
            isConnected = false,
            inputType = HardwareInputType.HDMI_3
        ),
        TvInputPillItem(
            id = "input_av",
            label = "AV",
            status = "No signal",
            isConnected = false,
            inputType = HardwareInputType.AV
        ),
        TvInputPillItem(
            id = "input_usb",
            label = "USB",
            status = "Not connected",
            isConnected = false,
            inputType = HardwareInputType.USB
        ),
        TvInputPillItem(
            id = "input_cast",
            label = "Screen cast",
            status = "Not connected",
            isConnected = false,
            inputType = HardwareInputType.SCREEN_CAST
        )
    )
}
