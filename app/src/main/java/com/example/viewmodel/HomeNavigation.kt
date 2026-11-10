package com.example.viewmodel

import com.example.model.RailDestination

// Only transient navigation is reset. Saved preferences and a confirmed save in flight survive.
internal fun LauncherUiState.returnedHome(): LauncherUiState = copy(
    selectedRail = RailDestination.HOME,
    isInputFullScreen = false,
    isQuickSettingsOpen = false,
    isHeroSelectorOpen = false,
    isAddToWatchDialogOpen = false,
    isAllAppsOverlayOpen = false,
    isSearchOverlayOpen = false,
    homeReturnSequence = homeReturnSequence + 1
)
