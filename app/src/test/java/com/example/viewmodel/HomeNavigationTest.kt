package com.example.viewmodel

import com.example.model.HeroSource
import com.example.model.RailDestination
import org.junit.Assert.*
import org.junit.Test

class HomeNavigationTest {
    @Test fun homeDismissesEveryTransientScreenWithoutChangingUserData() {
        val source = HeroSource("input", "input_hdmi2", "HDMI 2")
        val before = LauncherUiState(
            selectedRail = RailDestination.WALLPAPER,
            isInputFullScreen = true,
            isQuickSettingsOpen = true,
            isHeroSelectorOpen = true,
            isAddToWatchDialogOpen = true,
            isAllAppsOverlayOpen = true,
            isSearchOverlayOpen = true,
            heroSource = source,
            showLeftSidebar = false,
            hasSavedHomeSelection = true,
            isSavingHomeApps = true
        )
        val home = before.returnedHome()
        assertEquals(RailDestination.HOME, home.selectedRail)
        assertFalse(home.isInputFullScreen || home.isQuickSettingsOpen || home.isHeroSelectorOpen ||
            home.isAddToWatchDialogOpen || home.isAllAppsOverlayOpen || home.isSearchOverlayOpen)
        assertEquals(before, home.copy(
            selectedRail = before.selectedRail,
            isInputFullScreen = true, isQuickSettingsOpen = true, isHeroSelectorOpen = true,
            isAddToWatchDialogOpen = true, isAllAppsOverlayOpen = true, isSearchOverlayOpen = true,
            homeReturnSequence = before.homeReturnSequence
        ))
    }

    @Test fun repeatedHomeStillRequestsFocusWhenAlreadyOnHome() {
        val first = LauncherUiState().returnedHome()
        val second = first.returnedHome()
        assertEquals(first.homeReturnSequence + 1, second.homeReturnSequence)
        assertEquals(first, second.copy(homeReturnSequence = first.homeReturnSequence))
    }
}
