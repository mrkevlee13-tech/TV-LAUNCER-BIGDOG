package com.example.data

import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.B2SampleCatalog
import com.example.model.HardwareInputType
import com.example.model.InstalledTvApp
import com.example.model.QuickSettingTileItem
import com.example.model.TvInputPillItem
import com.example.model.WatchItem
import com.example.model.WatchTab

/**
 * Provides real Android TV system integrations:
 * 1. Discovers ALL launchable APKs and default built-in apps on the TV (Leanback + Launcher +
 *    getInstalledApplications fallback) with their actual installed banner and icon drawables.
 * 2. Reads Android TV Channels (preview_program & watch_next_program) and merges them with
 *    Last Played history and user-customized Watch Shelf items (Continue, Next episode, To Watch / Favorites).
 * 3. Reads real hardware states for HDMI 1–3, AV, USB storage, Screen cast, Wi-Fi, and Bluetooth.
 */
object TvSystemRepository {

    private val EXCLUDED_PACKAGES = setOf(
        "com.google.android.tvlauncher",
        "com.google.android.apps.tv.launcherx",
        "com.mitv.tvhome",
        "com.xiaomi.tv.home",
        "com.xiaomi.mitv.systemui",
        "com.xiaomi.tvqs",
        "com.xiaomi.mitv.smartpanel",
        "com.android.Fallback",
        "com.android.traceur"
    )

    /**
     * Queries PackageManager for all launchable APKs and default apps built into the TV:
     * 1. Intent.CATEGORY_LEANBACK_LAUNCHER
     * 2. Intent.CATEGORY_LAUNCHER
     * 3. PackageManager.getInstalledApplications fallback for any sideloaded APK with a launch intent.
     */
    fun queryAllInstalledTvApps(
        context: Context,
        pinnedPackages: List<String>
    ): List<InstalledTvApp> {
        val pm = context.packageManager
        val selfPackage = context.packageName

        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val standardIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val leanbackResolved = queryIntentActivitiesCompat(pm, leanbackIntent)
        val standardResolved = queryIntentActivitiesCompat(pm, standardIntent)

        val appsByPackage = LinkedHashMap<String, InstalledTvApp>()

        fun processResolveList(list: List<ResolveInfo>, isLeanbackCategory: Boolean) {
            for (resolveInfo in list) {
                val activityInfo = resolveInfo.activityInfo ?: continue
                val appInfo = activityInfo.applicationInfo ?: continue
                val pkgName = activityInfo.packageName ?: continue

                if (pkgName == selfPackage || pkgName in EXCLUDED_PACKAGES) continue
                if (appsByPackage.containsKey(pkgName)) continue

                val label = runCatching {
                    resolveInfo.loadLabel(pm).toString().trim()
                }.getOrDefault("").ifEmpty {
                    runCatching { pm.getApplicationLabel(appInfo).toString().trim() }
                        .getOrDefault(pkgName.substringAfterLast('.'))
                }

                val bannerDrawable = runCatching {
                    activityInfo.loadBanner(pm)
                        ?: appInfo.loadBanner(pm)
                        ?: pm.getApplicationBanner(appInfo)
                }.getOrNull()

                val iconDrawable = runCatching {
                    activityInfo.loadIcon(pm)
                        ?: appInfo.loadIcon(pm)
                        ?: pm.getApplicationIcon(appInfo)
                }.getOrNull()

                val bannerBitmap = bannerDrawable?.let {
                    drawableToImageBitmap(it, targetWidth = 320, targetHeight = 180)
                }
                val iconBitmap = iconDrawable?.let {
                    drawableToImageBitmap(it, targetWidth = 144, targetHeight = 144)
                }

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

                appsByPackage[pkgName] = InstalledTvApp(
                    id = "app_$pkgName",
                    name = label,
                    packageName = pkgName,
                    activityName = activityInfo.name ?: "",
                    isLeanback = isLeanbackCategory,
                    bannerBitmap = bannerBitmap,
                    iconBitmap = iconBitmap,
                    isPinned = pkgName in pinnedPackages,
                    isSystemApp = isSystem
                )
            }
        }

        processResolveList(leanbackResolved, isLeanbackCategory = true)
        processResolveList(standardResolved, isLeanbackCategory = false)

        // 3. Fallback scan of all installed applications so sideloaded APKs (like Movie Box Pro)
        // with custom entry intents are always discovered.
        val installedAppInfos = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }
        }.getOrDefault(emptyList())

        for (appInfo in installedAppInfos) {
            val pkgName = appInfo.packageName ?: continue
            if (pkgName == selfPackage || pkgName in EXCLUDED_PACKAGES) continue
            if (appsByPackage.containsKey(pkgName)) continue

            val leanbackLaunch = runCatching { pm.getLeanbackLaunchIntentForPackage(pkgName) }.getOrNull()
            val standardLaunch = runCatching { pm.getLaunchIntentForPackage(pkgName) }.getOrNull()
            if (leanbackLaunch == null && standardLaunch == null) continue

            val label = runCatching {
                pm.getApplicationLabel(appInfo).toString().trim()
            }.getOrDefault(pkgName.substringAfterLast('.'))

            val bannerDrawable = runCatching {
                appInfo.loadBanner(pm) ?: pm.getApplicationBanner(appInfo)
            }.getOrNull()
            val iconDrawable = runCatching {
                appInfo.loadIcon(pm) ?: pm.getApplicationIcon(appInfo)
            }.getOrNull()

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

            appsByPackage[pkgName] = InstalledTvApp(
                id = "app_$pkgName",
                name = label,
                packageName = pkgName,
                activityName = (leanbackLaunch ?: standardLaunch)?.component?.className ?: "",
                isLeanback = leanbackLaunch != null,
                bannerBitmap = bannerDrawable?.let { drawableToImageBitmap(it, 320, 180) },
                iconBitmap = iconDrawable?.let { drawableToImageBitmap(it, 144, 144) },
                isPinned = pkgName in pinnedPackages,
                isSystemApp = isSystem
            )
        }

        val allApps = appsByPackage.values.toList()
        val pinnedIndexMap = pinnedPackages.withIndex().associate { it.value to it.index }

        return allApps.sortedWith(
            compareBy<InstalledTvApp> { app ->
                pinnedIndexMap[app.packageName] ?: Int.MAX_VALUE
            }
                .thenBy { it.isSystemApp }
                .thenByDescending { it.isLeanback }
                .thenBy { it.name.lowercase() }
        )
    }

    /**
     * Finds Movie Box Pro or the user's chosen Hero app from the installed apps list.
     */
    fun resolveHeroApp(
        installedApps: List<InstalledTvApp>,
        preferredPackage: String
    ): InstalledTvApp? {
        return installedApps.firstOrNull { it.packageName == preferredPackage }
            ?: installedApps.firstOrNull {
                it.packageName.contains("moviebox", ignoreCase = true) ||
                    it.name.contains("Movie Box", ignoreCase = true) ||
                    it.name.contains("MovieBox", ignoreCase = true)
            }
    }

    /**
     * Builds the NVIDIA Shield / Android TV style Watch Panel items for:
     * - Continue (Last Played apps + Watch Next + custom Continue items)
     * - Next episode (Preview Programs / Watch Next next-episodes + custom Next Episode items)
     * - To Watch (User Favorites & To Watch list + pinned media apps)
     */
    fun buildWatchPanelItems(
        context: Context,
        installedApps: List<InstalledTvApp>,
        recentPackages: List<String>,
        customWatchItems: List<WatchItem>
    ): List<WatchItem> {
        val result = mutableListOf<WatchItem>()
        val appByPkg = installedApps.associateBy { it.packageName }

        // 1. Enrich any user-created custom items (Continue, Next episode, To Watch / Favorites)
        // with the real installed app's banner and icon artwork
        val enrichedCustom = customWatchItems.map { item ->
            val matchedApp = item.targetPackage?.let { appByPkg[it] }
                ?: installedApps.firstOrNull { app ->
                    item.subtitle.contains(app.name, ignoreCase = true) ||
                        item.title.contains(app.name, ignoreCase = true)
                }
            if (matchedApp != null) {
                item.copy(
                    targetPackage = matchedApp.packageName,
                    bannerBitmap = matchedApp.bannerBitmap,
                    iconBitmap = matchedApp.iconBitmap
                )
            } else {
                item
            }
        }
        result.addAll(enrichedCustom)

        // 2. Query Android TV Watch Next & Preview Program channels (published by Movie Box Pro, SmartTube, etc.)
        val providerPrograms = querySystemTvProviderPrograms(context, appByPkg)
        result.addAll(providerPrograms)

        // 3. Populate "Continue" from Recent / Last Played apps first, then installed media apps
        val recentApps = recentPackages.mapNotNull { appByPkg[it] }
        val mediaApps = (recentApps + installedApps.filter {
            it.packageName != "com.android.settings"
        }).distinctBy { it.packageName }

        val existingContinueCount = result.count { it.tab == WatchTab.CONTINUE }
        if (existingContinueCount < 6) {
            mediaApps.forEachIndexed { index, app ->
                if (result.count { it.tab == WatchTab.CONTINUE } >= 6) return@forEachIndexed
                if (result.any { it.tab == WatchTab.CONTINUE && it.targetPackage == app.packageName && !it.isManualEntry }) {
                    return@forEachIndexed
                }
                val isRecent = app.packageName in recentPackages
                val gradient = B2SampleCatalog.cardGradients[index % B2SampleCatalog.cardGradients.size]
                result.add(
                    WatchItem(
                        id = "continue_app_${app.packageName}",
                        title = app.name,
                        subtitle = if (isRecent) "Last played · Resume" else "Installed on TV · Open",
                        progress = if (isRecent) 0.65f else 0f,
                        gradientStart = gradient.first,
                        gradientEnd = gradient.second,
                        tab = WatchTab.CONTINUE,
                        targetPackage = app.packageName,
                        bannerBitmap = app.bannerBitmap,
                        iconBitmap = app.iconBitmap
                    )
                )
            }
        }

        // 4. Populate "Next episode" with remaining slots from Movie Box Pro / media apps
        if (result.count { it.tab == WatchTab.NEXT_EPISODE } < 6) {
            val movieBoxApp = resolveHeroApp(installedApps, B2SampleCatalog.MOVIE_BOX_PRO_PACKAGE)
            val orderedForNext = buildList {
                if (movieBoxApp != null) add(movieBoxApp)
                addAll(mediaApps.filter { it.packageName != movieBoxApp?.packageName })
            }
            orderedForNext.forEachIndexed { index, app ->
                if (result.count { it.tab == WatchTab.NEXT_EPISODE } >= 6) return@forEachIndexed
                val gradient = B2SampleCatalog.cardGradients[(index + 1) % B2SampleCatalog.cardGradients.size]
                result.add(
                    WatchItem(
                        id = "next_app_${app.packageName}",
                        title = app.name,
                        subtitle = "${app.name} · Next episode queue",
                        progress = 0.25f,
                        gradientStart = gradient.first,
                        gradientEnd = gradient.second,
                        tab = WatchTab.NEXT_EPISODE,
                        targetPackage = app.packageName,
                        bannerBitmap = app.bannerBitmap,
                        iconBitmap = app.iconBitmap
                    )
                )
            }
        }

        // 5. Populate "To Watch" (Favorites & Watchlist) with pinned/favorite apps if user hasn't added custom items yet
        if (result.none { it.tab == WatchTab.TO_WATCH }) {
            val favApps = installedApps.filter { it.isPinned }.ifEmpty { mediaApps.take(6) }
            favApps.take(6).forEachIndexed { index, app ->
                val gradient = B2SampleCatalog.cardGradients[(index + 2) % B2SampleCatalog.cardGradients.size]
                result.add(
                    WatchItem(
                        id = "fav_app_${app.packageName}",
                        title = app.name,
                        subtitle = "Favorite TV App · Press OK to launch",
                        progress = 0f,
                        gradientStart = gradient.first,
                        gradientEnd = gradient.second,
                        tab = WatchTab.TO_WATCH,
                        targetPackage = app.packageName,
                        bannerBitmap = app.bannerBitmap,
                        iconBitmap = app.iconBitmap
                    )
                )
            }
        }

        return result
    }

    /**
     * Reads both content://android.media.tv/watch_next_program and content://android.media.tv/preview_program
     * (used by Android TV / NVIDIA Shield home channels for apps like Movie Box Pro, SmartTube, YouTube, etc.).
     */
    private fun querySystemTvProviderPrograms(
        context: Context,
        appByPkg: Map<String, InstalledTvApp>
    ): List<WatchItem> {
        val items = mutableListOf<WatchItem>()

        fun queryUri(uriString: String, defaultTab: WatchTab) {
            runCatching {
                val uri = Uri.parse(uriString)
                val projection = arrayOf(
                    "_id",
                    "title",
                    "episode_title",
                    "intent_uri",
                    "package_name",
                    "last_playback_position_millis",
                    "duration_millis",
                    "watch_next_type"
                )
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val idIdx = cursor.getColumnIndex("_id")
                    val titleIdx = cursor.getColumnIndex("title")
                    val epIdx = cursor.getColumnIndex("episode_title")
                    val pkgIdx = cursor.getColumnIndex("package_name")
                    val intentIdx = cursor.getColumnIndex("intent_uri")
                    val posIdx = cursor.getColumnIndex("last_playback_position_millis")
                    val durIdx = cursor.getColumnIndex("duration_millis")
                    val typeIdx = cursor.getColumnIndex("watch_next_type")

                    var count = 0
                    while (cursor.moveToNext() && count < 8) {
                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else count.toLong()
                        val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                        if (title.isBlank()) continue
                        val epTitle = if (epIdx >= 0) cursor.getString(epIdx) else null
                        val pkg = if (pkgIdx >= 0) cursor.getString(pkgIdx) else null
                        val intentUri = if (intentIdx >= 0) cursor.getString(intentIdx) else null
                        val pos = if (posIdx >= 0) cursor.getLong(posIdx) else 0L
                        val dur = if (durIdx >= 0) cursor.getLong(durIdx) else 0L
                        val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else -1

                        val matchedApp = pkg?.let { appByPkg[it] }
                        val appLabel = matchedApp?.name
                            ?: pkg?.substringAfterLast('.')?.replaceFirstChar { it.uppercase() }
                            ?: "TV Channel"

                        val subtitleText = if (!epTitle.isNullOrBlank()) {
                            "$appLabel · $epTitle"
                        } else {
                            appLabel
                        }

                        val progress = if (dur > 0L) {
                            (pos.toFloat() / dur.toFloat()).coerceIn(0.05f, 1f)
                        } else {
                            0.25f
                        }

                        val tab = when (type) {
                            1 -> WatchTab.NEXT_EPISODE
                            3 -> WatchTab.TO_WATCH
                            else -> defaultTab
                        }
                        val gradient = B2SampleCatalog.cardGradients[count % B2SampleCatalog.cardGradients.size]

                        items.add(
                            WatchItem(
                                id = "tvprov_${defaultTab.name}_$id",
                                title = title,
                                subtitle = subtitleText,
                                progress = progress,
                                gradientStart = gradient.first,
                                gradientEnd = gradient.second,
                                tab = tab,
                                targetPackage = pkg,
                                deepLinkUri = intentUri,
                                bannerBitmap = matchedApp?.bannerBitmap,
                                iconBitmap = matchedApp?.iconBitmap
                            )
                        )
                        count++
                    }
                }
            }
        }

        queryUri("content://android.media.tv/watch_next_program", WatchTab.CONTINUE)
        queryUri("content://android.media.tv/preview_program", WatchTab.NEXT_EPISODE)
        return items
    }

    /**
     * Launches any installed TV application safely (Leanback intent first, then standard launcher intent).
     */
    fun launchInstalledApp(context: Context, app: InstalledTvApp): Result<Unit> {
        return runCatching {
            val pm = context.packageManager
            val launchIntent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
                ?: pm.getLaunchIntentForPackage(app.packageName)
                ?: if (app.activityName.isNotBlank()) {
                    Intent(Intent.ACTION_MAIN).apply {
                        component = ComponentName(app.packageName, app.activityName)
                    }
                } else {
                    null
                }
                ?: throw IllegalStateException("No launch intent found for ${app.packageName}")

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }

    fun launchPackageIfInstalled(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val pm = context.packageManager
        val intent = runCatching {
            pm.getLeanbackLaunchIntentForPackage(packageName)
                ?: pm.getLaunchIntentForPackage(packageName)
        }.getOrNull() ?: return false

        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Queries real hardware input connection states from TvInputManager, StorageManager/UsbManager,
     * and DisplayManager. Never fakes "Connected" or "Ready" when disconnected!
     */
    fun queryHardwareInputs(context: Context): List<TvInputPillItem> {
        val tvInputManager = runCatching {
            context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
        }.getOrNull()

        val systemInputs: List<TvInputInfo> = runCatching {
            tvInputManager?.tvInputList ?: emptyList()
        }.getOrDefault(emptyList())

        val hdmiInputs = systemInputs
            .filter { it.type == TvInputInfo.TYPE_HDMI }
            .sortedBy { it.id }

        val avInput = systemInputs.firstOrNull {
            it.type == TvInputInfo.TYPE_COMPOSITE ||
                it.type == TvInputInfo.TYPE_COMPONENT ||
                it.type == TvInputInfo.TYPE_OTHER
        }

        fun buildHdmiPill(
            index: Int,
            id: String,
            label: String,
            inputType: HardwareInputType
        ): TvInputPillItem {
            val matchedInfo = hdmiInputs.getOrNull(index)
                ?: systemInputs.firstOrNull {
                    it.id.contains("hdmi${index + 1}", ignoreCase = true) ||
                        it.id.contains("HW${index + 1}", ignoreCase = true)
                }

            if (matchedInfo != null && tvInputManager != null) {
                val state = runCatching {
                    tvInputManager.getInputState(matchedInfo.id)
                }.getOrDefault(TvInputManager.INPUT_STATE_DISCONNECTED)
                val connected = state == TvInputManager.INPUT_STATE_CONNECTED ||
                    state == TvInputManager.INPUT_STATE_CONNECTED_STANDBY
                return TvInputPillItem(
                    id = id,
                    label = label,
                    status = if (connected) "Connected" else "No signal",
                    isConnected = connected,
                    inputType = inputType,
                    tvInputId = matchedInfo.id
                )
            }

            return TvInputPillItem(
                id = id,
                label = label,
                status = "No signal",
                isConnected = false,
                inputType = inputType,
                tvInputId = null
            )
        }

        val avConnected = if (avInput != null && tvInputManager != null) {
            runCatching {
                val st = tvInputManager.getInputState(avInput.id)
                st == TvInputManager.INPUT_STATE_CONNECTED || st == TvInputManager.INPUT_STATE_CONNECTED_STANDBY
            }.getOrDefault(false)
        } else {
            false
        }

        val (usbConnected, usbStatusText) = checkRealUsbConnection(context)
        val (castConnected, castStatusText) = checkRealCastState(context)

        return listOf(
            buildHdmiPill(0, "input_hdmi1", "HDMI 1", HardwareInputType.HDMI_1),
            buildHdmiPill(1, "input_hdmi2", "HDMI 2", HardwareInputType.HDMI_2),
            buildHdmiPill(2, "input_hdmi3", "HDMI 3", HardwareInputType.HDMI_3),
            TvInputPillItem(
                id = "input_av",
                label = "AV",
                status = if (avConnected) "Connected" else "No signal",
                isConnected = avConnected,
                inputType = HardwareInputType.AV,
                tvInputId = avInput?.id
            ),
            TvInputPillItem(
                id = "input_usb",
                label = "USB",
                status = usbStatusText,
                isConnected = usbConnected,
                inputType = HardwareInputType.USB,
                tvInputId = null
            ),
            TvInputPillItem(
                id = "input_cast",
                label = "Screen cast",
                status = castStatusText,
                isConnected = castConnected,
                inputType = HardwareInputType.SCREEN_CAST,
                tvInputId = null
            )
        )
    }

    fun queryQuickSettingTiles(context: Context, wifiDisplayName: String): List<QuickSettingTileItem> {
        val (usbConnected, usbStatusText) = checkRealUsbConnection(context)
        val (castConnected, castStatusText) = checkRealCastState(context)
        val (btEnabled, btStatusText) = checkRealBluetoothState(context)
        val isNetworkConnected = wifiDisplayName != "Disconnected"

        return listOf(
            QuickSettingTileItem(
                id = "qs_tile_wifi",
                title = "Wi-Fi",
                subtitle = wifiDisplayName,
                isActive = isNetworkConnected,
                settingsAction = Settings.ACTION_WIFI_SETTINGS
            ),
            QuickSettingTileItem(
                id = "qs_tile_bt",
                title = "Bluetooth",
                subtitle = btStatusText,
                isActive = btEnabled,
                settingsAction = Settings.ACTION_BLUETOOTH_SETTINGS
            ),
            QuickSettingTileItem(
                id = "qs_tile_cast",
                title = "Cast",
                subtitle = castStatusText,
                isActive = castConnected,
                settingsAction = Settings.ACTION_CAST_SETTINGS
            ),
            QuickSettingTileItem(
                id = "qs_tile_usb",
                title = "USB",
                subtitle = usbStatusText,
                isActive = usbConnected,
                settingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            )
        )
    }

    fun checkRealUsbConnection(context: Context): Pair<Boolean, String> {
        return runCatching {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            val removableMounted = storageManager?.storageVolumes?.firstOrNull { vol ->
                vol.isRemovable && vol.state == Environment.MEDIA_MOUNTED
            }
            if (removableMounted != null) {
                val desc = removableMounted.getDescription(context)?.takeIf { it.isNotBlank() }
                    ?: "Drive connected"
                return@runCatching true to desc
            }

            val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            val hasMassStorageDevice = usbManager?.deviceList?.values?.any { device ->
                if (device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE) {
                    true
                } else {
                    (0 until device.interfaceCount).any { idx ->
                        device.getInterface(idx).interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE
                    }
                }
            } == true

            if (hasMassStorageDevice) {
                true to "Drive connected"
            } else {
                false to "Not connected"
            }
        }.getOrDefault(false to "Not connected")
    }

    fun checkRealCastState(context: Context): Pair<Boolean, String> {
        return runCatching {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val presentationDisplays = dm?.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            if (!presentationDisplays.isNullOrEmpty()) {
                true to "Casting active"
            } else {
                false to "Not connected"
            }
        }.getOrDefault(false to "Not connected")
    }

    fun checkRealBluetoothState(context: Context): Pair<Boolean, String> {
        return runCatching {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val isOn = bm?.adapter?.isEnabled == true
            if (isOn) {
                true to "On"
            } else {
                false to "Off"
            }
        }.getOrDefault(false to "Off")
    }

    @Suppress("DEPRECATION")
    fun resolveWifiDisplayName(context: Context): String {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork ?: return@runCatching "Disconnected"
            val caps = cm.getNetworkCapabilities(activeNet) ?: return@runCatching "Disconnected"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val rawSsid = wm?.connectionInfo?.ssid?.trim('"')
                    if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>") {
                        rawSsid
                    } else {
                        "Wi-Fi Connected"
                    }
                }
                else -> "Connected"
            }
        }.getOrDefault("Disconnected")
    }

    fun activateHardwareInput(context: Context, pill: TvInputPillItem): String {
        return when (pill.inputType) {
            HardwareInputType.HDMI_1,
            HardwareInputType.HDMI_2,
            HardwareInputType.HDMI_3,
            HardwareInputType.AV -> {
                val candidateIds = buildList {
                    if (!pill.tvInputId.isNullOrBlank()) add(pill.tvInputId)
                    when (pill.inputType) {
                        HardwareInputType.HDMI_1 -> {
                            add("com.xiaomi.mitv.tvplayer/.hdmi1")
                            add("com.droidlogic.tvinput/.services.Hdmi1InputService/HW5")
                        }
                        HardwareInputType.HDMI_2 -> {
                            add("com.xiaomi.mitv.tvplayer/.hdmi2")
                            add("com.droidlogic.tvinput/.services.Hdmi2InputService/HW6")
                        }
                        HardwareInputType.HDMI_3 -> {
                            add("com.xiaomi.mitv.tvplayer/.hdmi3")
                            add("com.droidlogic.tvinput/.services.Hdmi3InputService/HW7")
                        }
                        HardwareInputType.AV -> {
                            add("com.xiaomi.mitv.tvplayer/.av")
                            add("com.droidlogic.tvinput/.services.Av1InputService/HW1")
                        }
                        else -> Unit
                    }
                }

                for (inputId in candidateIds) {
                    val passthroughUri: Uri = TvContract.buildChannelUriForPassthroughInput(inputId)
                    val viewIntent = Intent(Intent.ACTION_VIEW, passthroughUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val launched = runCatching {
                        context.startActivity(viewIntent)
                        true
                    }.getOrDefault(false)
                    if (launched) {
                        return "Switching to ${pill.label}..."
                    }
                }

                val fallbackLaunched = runCatching {
                    context.packageManager.getLeanbackLaunchIntentForPackage("com.xiaomi.mitv.tvplayer")
                        ?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                            true
                        } ?: false
                }.getOrDefault(false)

                if (fallbackLaunched) {
                    "Opened TV Input Selector for ${pill.label}"
                } else {
                    "${pill.label}: ${pill.status}"
                }
            }

            HardwareInputType.USB -> {
                val storageIntent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val launched = runCatching {
                    context.startActivity(storageIntent)
                    true
                }.getOrDefault(false) || runCatching {
                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                    true
                }.getOrDefault(false)

                if (launched) "Opened USB & Storage" else "USB: ${pill.status}"
            }

            HardwareInputType.SCREEN_CAST -> {
                val castIntent = Intent(Settings.ACTION_CAST_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val launched = runCatching {
                    context.startActivity(castIntent)
                    true
                }.getOrDefault(false) || runCatching {
                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                    true
                }.getOrDefault(false)

                if (launched) "Opened Screen Cast settings" else "Screen Cast: ${pill.status}"
            }
        }
    }

    fun loadCustomWallpaperBitmap(context: Context, uriString: String?): ImageBitmap? {
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = Uri.parse(uriString)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > 1920 || boundsOptions.outHeight / sampleSize > 1080) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)?.asImageBitmap()
            }
        }.getOrNull()
    }

    private fun queryIntentActivitiesCompat(pm: PackageManager, intent: Intent): List<ResolveInfo> {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        }.getOrDefault(emptyList())
    }

    private fun drawableToImageBitmap(
        drawable: Drawable,
        targetWidth: Int,
        targetHeight: Int
    ): ImageBitmap? {
        return runCatching {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                return@runCatching drawable.bitmap.asImageBitmap()
            }
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: targetWidth
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: targetHeight
            val bmp = Bitmap.createBitmap(
                width.coerceIn(64, 640),
                height.coerceIn(64, 360),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp.asImageBitmap()
        }.getOrNull()
    }
}
