package com.tensormind.nudge.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MainContainer(
    initialTab: Int = 0,
    sharedText: String? = null,
    onClearSharedText: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    androidx.compose.runtime.LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            selectedTab = 0
        }
    }

    Crossfade(
        targetState = selectedTab,
        label = "MainTabTransition",
        modifier = modifier
    ) { tab ->
        when (tab) {
            0 -> HomeScreen(
                selectedTab = selectedTab,
                sharedText = sharedText,
                onClearSharedText = onClearSharedText,
                onTabSelected = { selectedTab = it }
            )
            1 -> HistoryScreen(
                onTabSelected = { selectedTab = it }
            )
            2 -> FamilyScreen(
                onTabSelected = { selectedTab = it }
            )
        }
    }
}
