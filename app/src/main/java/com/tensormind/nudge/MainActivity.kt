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

import android.content.Context
import com.tensormind.nudge.ai.LanguageManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NudgeTheme {
                val prefs = remember { getSharedPreferences("nudge_prefs", Context.MODE_PRIVATE) }
                var showHomeScreen by remember {
                    mutableStateOf(prefs.getBoolean("has_completed_onboarding", false))
                }

                if (showHomeScreen) {
                    MainContainer()
                } else {
                    OnboardingContainer(
                        onComplete = {
                            prefs.edit()
                                .putBoolean("has_completed_onboarding", true)
                                .putString("selected_lang", LanguageManager.currentLanguageCode)
                                .apply()
                            showHomeScreen = true
                        },
                        onFamilyMemberSelected = {
                            prefs.edit()
                                .putBoolean("has_completed_onboarding", true)
                                .putString("selected_lang", LanguageManager.currentLanguageCode)
                                .apply()
                            showHomeScreen = true
                        }
                    )
                }
            }
        }
    }
}
