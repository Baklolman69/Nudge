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
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    Crossfade(
        targetState = selectedTab,
        label = "MainTabTransition",
        modifier = modifier
    ) { tab ->
        when (tab) {
            0 -> HomeScreen(
                selectedTab = selectedTab,
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
