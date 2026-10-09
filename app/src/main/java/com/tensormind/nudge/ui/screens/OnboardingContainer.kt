package com.tensormind.nudge.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun OnboardingContainer(
    onComplete: () -> Unit = {},
    onFamilyMemberSelected: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> SplashScreen1(
                onNext = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
            1 -> SplashScreen2(
                onGetStarted = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            )
            2 -> SplashScreen3(
                onStartUsingNudge = {
                    onComplete()
                },
                onFamilyMemberClick = {
                    onFamilyMemberSelected()
                }
            )
        }
    }
}
