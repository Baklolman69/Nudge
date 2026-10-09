package com.tensormind.nudge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tensormind.nudge.ai.LanguageManager
import com.tensormind.nudge.diagnostics.FamilyRepository
import com.tensormind.nudge.diagnostics.NudgeActivityRepository
import com.tensormind.nudge.ui.screens.MainContainer
import com.tensormind.nudge.ui.screens.OnboardingContainer
import com.tensormind.nudge.ui.theme.NudgeTheme

class MainActivity : ComponentActivity() {

    private var sharedTextState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("nudge_prefs", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("selected_lang", "en") ?: "en"
        LanguageManager.setLanguage(savedLang)

        // Initialize persistent repositories
        NudgeActivityRepository.initialize(this)
        FamilyRepository.initialize(this)

        handleIncomingIntent(intent)

        setContent {
            NudgeTheme {
                val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
                var showHomeScreen by remember {
                    mutableStateOf(hasCompletedOnboarding || sharedTextState != null)
                }

                LaunchedEffect(sharedTextState) {
                    if (sharedTextState != null) {
                        showHomeScreen = true
                    }
                }

                if (showHomeScreen) {
                    MainContainer(
                        sharedText = sharedTextState,
                        onClearSharedText = { sharedTextState = null }
                    )
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                sharedTextState = text
            }
        }
    }
}
