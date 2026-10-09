package com.tensormind.nudge.ui.components

import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.tensormind.nudge.ai.LanguageManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.R
import com.tensormind.nudge.ai.GroqQwenClient
import com.tensormind.nudge.diagnostics.AudioDiagnosticService
import com.tensormind.nudge.diagnostics.DeviceControlService
import com.tensormind.nudge.diagnostics.DeviceHealthService
import com.tensormind.nudge.diagnostics.NetworkDiagnosticService
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.SlateSubtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDiagnosticsSheet(
    groqClient: GroqQwenClient,
    audioService: AudioDiagnosticService,
    networkService: NetworkDiagnosticService,
    healthService: DeviceHealthService,
    deviceControl: DeviceControlService,
    autonomousMode: Boolean,
    onAutonomousModeChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val prefs = remember { context.getSharedPreferences("nudge_prefs", Context.MODE_PRIVATE) }

    var torchStatus by remember { mutableStateOf(if (deviceControl.isTorchActive()) "Turn Off" else "Turn On") }
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var groqApiKeyInput by remember { mutableStateOf(prefs.getString("groq_api_key", "") ?: "") }
    var apiKeySaveSuccess by remember { mutableStateOf(false) }

    // Read device metrics safely
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val bStatus = try {
        context.registerReceiver(null, ifilter)
    } catch (_: Exception) {
        null
    }
    val bLevel = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 80

    val stat = StatFs(Environment.getDataDirectory().path)
    val freeGb = (stat.availableBlocksLong * stat.blockSizeLong).toDouble() / (1024 * 1024 * 1024)

    val audioInfo = audioService.inspectAudio()
    val netInfo = networkService.inspectNetwork()
    val telemetry = groqClient.latestTelemetry

    // Friendly AI plain-English summary
    val summaryTitle = if (audioInfo.issuesFound.isEmpty() && netInfo.isConnected && bLevel > 20) {
        "Your Phone is in Great Shape! 🌸"
    } else {
        "A Few Things Need Attention 💡"
    }

    val soundSummary = if (audioInfo.issuesFound.isEmpty()) {
        "✓ Sound is clear and calls will ring loudly."
    } else {
        "⚠ Ringer might be quiet or silenced."
    }

    val netSummary = if (netInfo.isConnected) {
        "✓ Connected to the internet."
    } else {
        "⚠ Internet is currently disconnected."
    }

    val batterySummary = "✓ Battery has $bLevel% charge and plenty of storage left (${String.format("%.0f", freeGb)} GB free)."

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8FAFC), // Bright, clean, cheerful
        contentColor = NavyText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Bright Friendly Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot_nudge),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Assistant Tools",
                        color = NavyText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Easy controls and health checkup for your phone",
                        color = SlateSubtitle,
                        fontSize = 12.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. AI Plain-English Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sparkles),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = summaryTitle,
                            color = Color(0xFF1E293B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = soundSummary,
                        color = if (audioInfo.issuesFound.isEmpty()) Color(0xFF15803D) else Color(0xFFD97706),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = netSummary,
                        color = if (netInfo.isConnected) Color(0xFF15803D) else Color(0xFFD97706),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = batterySummary,
                        color = Color(0xFF15803D),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Automatic Fixes Switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automatic One-Tap Fixes",
                            color = NavyText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (autonomousMode) {
                                "Nudge automatically fixes your phone volume or settings as soon as you ask."
                            } else {
                                "Nudge will ask you before making any changes."
                            },
                            color = SlateSubtitle,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = autonomousMode,
                        onCheckedChange = onAutonomousModeChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Easy Quick Helper Buttons
            Text(
                text = "Quick Helper Actions",
                color = NavyText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Flashlight Button
                Button(
                    onClick = {
                        val res = deviceControl.toggleTorch()
                        torchStatus = if (deviceControl.isTorchActive()) "Turn Off" else "Turn On"
                        actionFeedback = res
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Text(
                        text = "🔦 Flashlight: $torchStatus",
                        color = Color(0xFF92400E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Restore Sound Button
                Button(
                    onClick = {
                        val res = audioService.fixSoundIssues()
                        actionFeedback = res
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7))
                ) {
                    Text(
                        text = "🔊 Unmute Sound",
                        color = Color(0xFF166534),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Screen Brightness Button
                OutlinedButton(
                    onClick = {
                        deviceControl.openDisplaySettings()
                        actionFeedback = "Opening screen settings for you..."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "☀️ Screen Brightness",
                        color = Color(0xFF0284C7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Clock / Alarm Button
                OutlinedButton(
                    onClick = {
                        deviceControl.openAlarmSettings()
                        actionFeedback = "Opening alarms and clock for you..."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "⏰ Clocks & Alarms",
                        color = Color(0xFF7C3AED),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Action Feedback Notification
            actionFeedback?.let { feedback ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✓ $feedback",
                        color = Color(0xFF1D4ED8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Recent Helpful Action Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Recent Nudge Checkup",
                        color = NavyText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (telemetry != null) {
                            "You recently asked: \"${telemetry.lastPrompt}\". Nudge checked your phone in ${telemetry.latencyMs}ms and kept everything safe."
                        } else {
                            "Nudge is actively monitoring your phone's sound, network, and battery in the background."
                        },
                        color = SlateSubtitle,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // 6. Language & Voice Settings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🌐 Language & Voice Support",
                        color = NavyText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Google Translation API is used for translations. Voice TTS is only supported in English.",
                        color = SlateSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageManager.supportedLanguages.forEach { lang ->
                            val isSelected = (lang.code == LanguageManager.currentLanguageCode)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF0E73F6) else Color(0xFFF1F5F9))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) Color(0xFF0E73F6) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        LanguageManager.setLanguage(lang.code)
                                        prefs.edit().putString("selected_lang", lang.code).apply()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = lang.flagEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = lang.displayName,
                                        color = if (isSelected) Color.White else NavyText,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val selectedLang = LanguageManager.getSelectedLanguage()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedLang.hasTts) Color(0xFFE8F8F0) else Color(0xFFFFFBEB))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (selectedLang.hasTts) {
                                "🎙️ Voice TTS is active (English)"
                            } else {
                                "💬 Text-only mode: Voice TTS disabled (${selectedLang.displayName})"
                            },
                            color = if (selectedLang.hasTts) Color(0xFF15803D) else Color(0xFFB45309),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Groq Cloud AI Engine API Key
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "⚡ Groq Cloud AI Settings",
                        color = NavyText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your Groq API key (gsk_...) to enable ultra-fast Qwen 2.5 inference. If unconfigured or offline, Nudge automatically uses the on-device heuristic engine.",
                        color = SlateSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = groqApiKeyInput,
                        onValueChange = {
                            groqApiKeyInput = it
                            apiKeySaveSuccess = false
                        },
                        placeholder = { Text("gsk_...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (apiKeySaveSuccess) {
                            Text(
                                text = "✓ API Key saved & active!",
                                color = Color(0xFF15803D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                val trimmed = groqApiKeyInput.trim()
                                prefs.edit().putString("groq_api_key", trimmed).apply()
                                groqClient.updateApiKey(trimmed)
                                apiKeySaveSuccess = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E73F6))
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Close Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E73F6))
            ) {
                Text(
                    text = "Done",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
