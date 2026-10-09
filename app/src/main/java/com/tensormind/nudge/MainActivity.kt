package com.tensormind.nudge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tensormind.nudge.ui.screens.MainContainer
import com.tensormind.nudge.ui.screens.OnboardingContainer
import com.tensormind.nudge.ui.theme.NudgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NudgeTheme {
                var showHomeScreen by remember { mutableStateOf(true) }

                if (showHomeScreen) {
                    MainContainer()
                } else {
                    OnboardingContainer(
                        onComplete = {
                            showHomeScreen = true
                        },
                        onFamilyMemberSelected = {
                            showHomeScreen = true
                        }
                    )
                }
            }
        }
    }
}
