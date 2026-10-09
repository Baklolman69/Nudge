package com.tensormind.nudge.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.R
import com.tensormind.nudge.ai.GroqQwenClient
import com.tensormind.nudge.diagnostics.AudioDiagnosticService
import com.tensormind.nudge.diagnostics.DeviceControlService
import com.tensormind.nudge.diagnostics.SmsDiagnosticService
import com.tensormind.nudge.diagnostics.DeviceHealthService
import com.tensormind.nudge.diagnostics.NetworkDiagnosticService
import com.tensormind.nudge.ui.components.DeveloperDiagnosticsSheet
import com.tensormind.nudge.ui.components.FullCheckupCard
import com.tensormind.nudge.ui.components.FullCheckupSheet
import com.tensormind.nudge.ui.components.NetworkTroubleshooterSheet
import com.tensormind.nudge.ui.components.NudgeBottomNavigation
import com.tensormind.nudge.ui.components.QuickActionCard
import com.tensormind.nudge.ui.components.ScamCheckerSheet
import com.tensormind.nudge.ui.components.SoundTroubleshooterSheet
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import com.tensormind.nudge.ui.components.ChatAssistantDialog
import com.tensormind.nudge.ui.components.VoiceListeningDialog
import kotlinx.coroutines.delay
import com.tensormind.nudge.ui.components.VoiceAssistantDialog
import com.tensormind.nudge.ui.theme.BgGradientEnd
import com.tensormind.nudge.ui.theme.BgGradientMid
import com.tensormind.nudge.ui.theme.BgGradientStart
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeTheme
import com.tensormind.nudge.ui.theme.SlateSubtitle
import com.tensormind.nudge.voice.NudgeVoiceManager

@Composable
fun HomeScreen(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Real Diagnostic & AI Services
    val audioService = remember { AudioDiagnosticService(context) }
    val networkService = remember { NetworkDiagnosticService(context) }
    val healthService = remember { DeviceHealthService(context) }
    val smsService = remember { SmsDiagnosticService(context) }
    val deviceControl = remember { DeviceControlService(context) }
    val groqClient = remember { GroqQwenClient() }
    val voiceManager = remember { NudgeVoiceManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    // Modal Sheet States
    var showSoundSheet by remember { mutableStateOf(false) }
    var showNetworkSheet by remember { mutableStateOf(false) }
    var showScamSheet by remember { mutableStateOf(false) }
    var showCheckupSheet by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }
    var showDevSheet by remember { mutableStateOf(false) }
    var autonomousMode by remember { mutableStateOf(true) }
    var greetingPromptIndex by remember { mutableIntStateOf(0) }

    val greetingPrompts = remember {
        listOf(
            "What do you need help with today?",
            "Can't hear calls? Tap to ask Nudge",
            "Internet slow? Tap here to troubleshoot",
            "Type your question in plain English!"
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            greetingPromptIndex = (greetingPromptIndex + 1) % greetingPrompts.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "homePulse")
    val mascotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotScale"
    )

    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    Scaffold(
        bottomBar = {
            NudgeBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BgGradientStart, BgGradientMid, BgGradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // 1. Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_nudge),
                        contentDescription = "Nudge Mascot Logo",
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nudge",
                            color = NavyText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Your phone's best friend. Always here to help.",
                            color = SlateSubtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { showDevSheet = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "DEV",
                                color = Color(0xFF22C55E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings & Developer Console",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { showDevSheet = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Mascot Speech Greeting Banner + Microphone Button (Interactive & Animated)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mascot Avatar in soft blue circle with breathing pulse
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(mascotScale)
                            .clip(CircleShape)
                            .background(Color(0xFFD9E9FF))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_mascot_nudge),
                            contentDescription = "Greeting Mascot",
                            modifier = Modifier.size(62.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Speech Bubble Card -> Opens ONLY Chat
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showChatSheet = true }
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(22.dp))
                            .border(1.dp, Color(0xFFE2EDFE), RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hello!",
                                    color = NavyText,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEBF5FF))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "💬 Tap to chat",
                                        color = Color(0xFF007AFF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            AnimatedContent(
                                targetState = greetingPrompts[greetingPromptIndex],
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(400)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                                },
                                label = "promptTransition"
                            ) { promptText ->
                                Text(
                                    text = promptText,
                                    color = NavyText,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Microphone FAB Button -> Opens ONLY Voice
                    IconButton(
                        onClick = {
                            showVoiceSheet = true
                        },
                        modifier = Modifier
                            .scale(micPulseScale)
                            .size(58.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = "Voice Assistant Microphone",
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Quick Action 2x2 Grid Cards
                // Row 1: Sound + Internet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    QuickActionCard(
                        iconRes = R.drawable.ic_volume_red,
                        title = "My phone is not making sound",
                        description = "Fix volume, DND, Bluetooth and more",
                        cardBgColor = Color(0xFFFFF0F2),
                        accentColor = Color(0xFFFF4D6D),
                        onClick = {
                            showSoundSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        iconRes = R.drawable.ic_wifi_blue,
                        title = "My internet isn't working",
                        description = "Check Wi-Fi, mobile data and connection issues",
                        cardBgColor = Color(0xFFEBF5FF),
                        accentColor = Color(0xFF007AFF),
                        onClick = {
                            showNetworkSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 2: Message Safety + Call Family
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    QuickActionCard(
                        iconRes = R.drawable.ic_shield_yellow,
                        title = "Is this message safe?",
                        description = "Check for scams, phishing and fake links",
                        cardBgColor = Color(0xFFFFFBEA),
                        accentColor = Color(0xFFFF9F00),
                        onClick = {
                            showScamSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        iconRes = R.drawable.ic_people_green,
                        title = "Call my family",
                        description = "Quickly reach your loved ones",
                        cardBgColor = Color(0xFFE8F8F0),
                        accentColor = Color(0xFF21A15C),
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Opening phone dialer...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Need a Full Checkup? Banner
                FullCheckupCard(
                    onRunCheckup = {
                        showCheckupSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Reassurance Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE5F1FF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You're not alone.",
                                color = NavyText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "We're here to keep your phone simple.",
                                color = SlateSubtitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.img_mascot_nudge),
                            contentDescription = "Mascot Banner",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Active Bottom Sheets
        if (showSoundSheet) {
            SoundTroubleshooterSheet(
                audioService = audioService,
                onDismiss = { showSoundSheet = false },
                onSpokenFeedback = { voiceManager.speak(it) }
            )
        }

        if (showNetworkSheet) {
            NetworkTroubleshooterSheet(
                networkService = networkService,
                onDismiss = { showNetworkSheet = false },
                onSpokenFeedback = { voiceManager.speak(it) }
            )
        }

        if (showScamSheet) {
            ScamCheckerSheet(
                groqClient = groqClient,
                smsService = smsService,
                onDismiss = { showScamSheet = false },
                onSpokenFeedback = { voiceManager.speak(it) }
            )
        }

        if (showCheckupSheet) {
            FullCheckupSheet(
                healthService = healthService,
                onDismiss = { showCheckupSheet = false }
            )
        }

        if (showChatSheet) {
            ChatAssistantDialog(
                groqClient = groqClient,
                audioService = audioService,
                onOpenScamSheet = {
                    showChatSheet = false
                    showScamSheet = true
                },
                onDismiss = { showChatSheet = false }
            )
        }

        if (showVoiceSheet) {
            VoiceListeningDialog(
                voiceManager = voiceManager,
                groqClient = groqClient,
                audioService = audioService,
                deviceControl = deviceControl,
                onDismiss = { showVoiceSheet = false }
            )
        }

        if (showDevSheet) {
            DeveloperDiagnosticsSheet(
                groqClient = groqClient,
                audioService = audioService,
                networkService = networkService,
                healthService = healthService,
                deviceControl = deviceControl,
                autonomousMode = autonomousMode,
                onAutonomousModeChanged = { autonomousMode = it },
                onDismiss = { showDevSheet = false }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 840)
@Composable
fun HomeScreenPreview() {
    NudgeTheme {
        HomeScreen()
    }
}
