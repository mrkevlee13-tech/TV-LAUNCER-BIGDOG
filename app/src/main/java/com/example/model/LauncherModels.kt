package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Thumb01End
import com.example.ui.theme.Thumb01Start
import com.example.ui.theme.Thumb02End
import com.example.ui.theme.Thumb02Start
import com.example.ui.theme.Thumb03End
import com.example.ui.theme.Thumb03Start
import com.example.ui.theme.Thumb04End
import com.example.ui.theme.Thumb04Start
import com.example.ui.theme.Thumb05End
import com.example.ui.theme.Thumb05Start
import com.example.ui.theme.Thumb06End
import com.example.ui.theme.Thumb06Start

enum class RailDestination(val id: String, val label: String) {
    HOME("rail_home", "Home"),
    MOVIES("rail_movies", "Movies"),
    ALL_APPS("rail_all_apps", "All apps"),
    INPUTS("rail_inputs", "Inputs"),
    SEARCH("rail_search", "Search"),
    SETTINGS("rail_settings", "Settings")
}

enum class WatchTab(val id: String, val title: String) {
    CONTINUE("tab_continue", "Continue"),
    NEXT_EPISODE("tab_next_episode", "Next episode"),
    TO_WATCH("tab_to_watch", "To Watch")
}

data class WatchItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val gradientStart: Color,
    val gradientEnd: Color,
    val tab: WatchTab
)

data class AppTileItem(
    val id: String,
    val name: String,
    val packageName: String,
    val iconColor: Color,
    val iconBgColor: Color,
    val isAllAppsCard: Boolean = false
)

data class TvInputPillItem(
    val id: String,
    val label: String,
    val status: String,
    val isConnected: Boolean,
    val inputUriOrAction: String
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
    val isHighlightedAccent: Boolean = false
)

object B2SampleCatalog {
    const val DEFAULT_MOVIE_BOX_PACKAGE = "com.movieboxpro.android"

    val initialWatchItems: List<WatchItem> = listOf(
        // Continue tab (matches B2 mockup 3x2 grid)
        WatchItem(
            id = "watch_01",
            title = "Title 01",
            subtitle = "SmartTube · 12 min left",
            progress = 0.68f,
            gradientStart = Thumb01Start,
            gradientEnd = Thumb01End,
            tab = WatchTab.CONTINUE
        ),
        WatchItem(
            id = "watch_02",
            title = "Title 02",
            subtitle = "Movie Box Pro · 41 min in",
            progress = 0.34f,
            gradientStart = Thumb02Start,
            gradientEnd = Thumb02End,
            tab = WatchTab.CONTINUE
        ),
        WatchItem(
            id = "watch_03",
            title = "Title 03",
            subtitle = "SmartTube · 3 min left",
            progress = 0.92f,
            gradientStart = Thumb03Start,
            gradientEnd = Thumb03End,
            tab = WatchTab.CONTINUE
        ),
        WatchItem(
            id = "watch_04",
            title = "Title 04",
            subtitle = "Movie Box Pro · S1 E4 next",
            progress = 0.15f,
            gradientStart = Thumb04Start,
            gradientEnd = Thumb04End,
            tab = WatchTab.CONTINUE
        ),
        WatchItem(
            id = "watch_05",
            title = "Title 05",
            subtitle = "Movie Box Pro · S2 E1 next",
            progress = 0.12f,
            gradientStart = Thumb05Start,
            gradientEnd = Thumb05End,
            tab = WatchTab.CONTINUE
        ),
        WatchItem(
            id = "watch_06",
            title = "Title 06",
            subtitle = "SmartTube · 1h 02m left",
            progress = 0.44f,
            gradientStart = Thumb06Start,
            gradientEnd = Thumb06End,
            tab = WatchTab.CONTINUE
        ),
        // Next episode tab
        WatchItem(
            id = "next_01",
            title = "Severance · S2 E03",
            subtitle = "Movie Box Pro · Up next",
            progress = 0.05f,
            gradientStart = Thumb05Start,
            gradientEnd = Thumb05End,
            tab = WatchTab.NEXT_EPISODE
        ),
        WatchItem(
            id = "next_02",
            title = "Shōgun · S1 E08",
            subtitle = "Movie Box Pro · Up next",
            progress = 0.05f,
            gradientStart = Thumb04Start,
            gradientEnd = Thumb04End,
            tab = WatchTab.NEXT_EPISODE
        ),
        WatchItem(
            id = "next_03",
            title = "The Last of Us · S2 E02",
            subtitle = "Movie Box Pro · Up next",
            progress = 0.05f,
            gradientStart = Thumb03Start,
            gradientEnd = Thumb03End,
            tab = WatchTab.NEXT_EPISODE
        ),
        // Manual To Watch tab
        WatchItem(
            id = "towatch_01",
            title = "Dune: Part Two (4K HDR)",
            subtitle = "Movie Box Pro · Manual To Watch",
            progress = 0.0f,
            gradientStart = Thumb02Start,
            gradientEnd = Thumb02End,
            tab = WatchTab.TO_WATCH
        ),
        WatchItem(
            id = "towatch_02",
            title = "Blade Runner 2049",
            subtitle = "VLC Network Stream · Manual To Watch",
            progress = 0.0f,
            gradientStart = Thumb01Start,
            gradientEnd = Thumb01End,
            tab = WatchTab.TO_WATCH
        ),
        WatchItem(
            id = "towatch_03",
            title = "4K OLED Calibration Demo",
            subtitle = "SmartTube · Manual To Watch",
            progress = 0.0f,
            gradientStart = Thumb06Start,
            gradientEnd = Thumb06End,
            tab = WatchTab.TO_WATCH
        )
    )

    val defaultAppsRow: List<AppTileItem> = listOf(
        AppTileItem(
            id = "app_vlc",
            name = "VLC",
            packageName = "org.videolan.vlc",
            iconColor = Color(0xFFE89B4C),
            iconBgColor = Color(0xFF2D231B)
        ),
        AppTileItem(
            id = "app_smarttube",
            name = "SmartTube",
            packageName = "com.liskovsoft.smarttubetv.beta",
            iconColor = Color(0xFFE05A5A),
            iconBgColor = Color(0xFF2E1C1E)
        ),
        AppTileItem(
            id = "app_files",
            name = "File Manager",
            packageName = "com.lonelycatgames.Xplore",
            iconColor = Color(0xFF5B9BD5),
            iconBgColor = Color(0xFF1B2531)
        ),
        AppTileItem(
            id = "app_vpn",
            name = "Tomato VPN",
            packageName = "com.tomatovpn.android",
            iconColor = Color(0xFFE06C5A),
            iconBgColor = Color(0xFF2D1E1C)
        ),
        AppTileItem(
            id = "app_bilibili",
            name = "Bilibili",
            packageName = "com.xiaodianshi.tv.yst",
            iconColor = Color(0xFF4AC2C2),
            iconBgColor = Color(0xFF192B2E)
        ),
        AppTileItem(
            id = "app_browser",
            name = "Browser",
            packageName = "com.TVBrowser.internet",
            iconColor = Color(0xFF7B93DB),
            iconBgColor = Color(0xFF1E2333)
        ),
        AppTileItem(
            id = "app_all_apps",
            name = "All apps",
            packageName = "",
            iconColor = Color(0xFF9AA3AD),
            iconBgColor = Color(0xFF1A1D21),
            isAllAppsCard = true
        )
    )

    val defaultInputsRow: List<TvInputPillItem> = listOf(
        TvInputPillItem(
            id = "input_hdmi1",
            label = "HDMI 1",
            status = "Connected",
            isConnected = true,
            inputUriOrAction = "content://android.media.tv/passthrough/com.xiaomi.mitv.tvplayer%2F.hdmi1"
        ),
        TvInputPillItem(
            id = "input_hdmi2",
            label = "HDMI 2",
            status = "No signal",
            isConnected = false,
            inputUriOrAction = "content://android.media.tv/passthrough/com.xiaomi.mitv.tvplayer%2F.hdmi2"
        ),
        TvInputPillItem(
            id = "input_hdmi3",
            label = "HDMI 3",
            status = "No signal",
            isConnected = false,
            inputUriOrAction = "content://android.media.tv/passthrough/com.xiaomi.mitv.tvplayer%2F.hdmi3"
        ),
        TvInputPillItem(
            id = "input_av",
            label = "AV",
            status = "No signal",
            isConnected = false,
            inputUriOrAction = "content://android.media.tv/passthrough/com.xiaomi.mitv.tvplayer%2F.av"
        ),
        TvInputPillItem(
            id = "input_usb",
            label = "USB",
            status = "Drive connected",
            isConnected = true,
            inputUriOrAction = "android.settings.INTERNAL_STORAGE_SETTINGS"
        ),
        TvInputPillItem(
            id = "input_cast",
            label = "Screen cast",
            status = "Ready",
            isConnected = true,
            inputUriOrAction = "android.settings.CAST_SETTINGS"
        )
    )
}
