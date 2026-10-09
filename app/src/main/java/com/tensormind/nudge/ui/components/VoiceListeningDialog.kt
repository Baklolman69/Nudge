package com.tensormind.nudge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import com.tensormind.nudge.R
import com.tensormind.nudge.ai.GroqQwenClient
import com.tensormind.nudge.diagnostics.AudioDiagnosticService
import com.tensormind.nudge.diagnostics.DeviceControlService
import com.tensormind.nudge.diagnostics.NudgeActivityRepository
import com.tensormind.nudge.ai.NudgeSkillsEngine
import com.tensormind.nudge.ai.SkillExecutionStep
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle
import com.tensormind.nudge.voice.NudgeVoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class VoiceState {
    LISTENING,
    THINKING,
    SPEAKING,
    IDLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceListeningDialog(
    voiceManager: NudgeVoiceManager,
    groqClient: GroqQwenClient,
    audioService: AudioDiagnosticService,
    deviceControl: DeviceControlService? = null,
    skillsEngine: NudgeSkillsEngine? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val devControl = deviceControl ?: remember { DeviceControlService(context) }
    val engine = skillsEngine ?: remember { NudgeSkillsEngine(context, audioService, devControl) }

    var voiceState by remember { mutableStateOf(VoiceState.LISTENING) }
    var userSpokenQuery by remember { mutableStateOf("") }
    var aiSpokenAnswer by remember { mutableStateOf("") }
    var executionSteps by remember { mutableStateOf<List<SkillExecutionStep>>(emptyList()) }
    var resolutionBadge by remember { mutableStateOf<String?>(null) }
    var thinkingStepIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val thinkingSteps = listOf(
        "Listening to your words...",
        "Understanding what happened...",
        "Consulting Qwen 2.5 AI...",
        "Figuring out the simplest fix..."
    )

    // Animated breathing / pulsing scale for mic orb
    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.35f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.6f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    // Dynamic thinking status text cycle
    LaunchedEffect(voiceState) {
        if (voiceState == VoiceState.THINKING) {
            thinkingStepIndex = 0
            while (voiceState == VoiceState.THINKING) {
                delay(1300)
                thinkingStepIndex = (thinkingStepIndex + 1) % thinkingSteps.size
            }
        }
    }

    fun handleUserVoiceQuery(speech: String) {
        if (speech.isBlank()) {
            voiceState = VoiceState.IDLE
            aiSpokenAnswer = "I didn't quite catch that. Please tap the microphone and speak again."
            return
        }

        userSpokenQuery = speech
        voiceState = VoiceState.THINKING

        scope.launch {
            val executionResult = engine.executeAutonomousWorkflow(speech)
            aiSpokenAnswer = executionResult.finalResolutionReport
            executionSteps = executionResult.steps
            resolutionBadge = executionResult.badgeTitle
            voiceState = VoiceState.SPEAKING
            voiceManager.speak(executionResult.finalResolutionReport)
        }
    }

    fun startListening() {
        voiceManager.stopSpeaking()
        userSpokenQuery = ""
        aiSpokenAnswer = ""
        voiceState = VoiceState.LISTENING

        voiceManager.startListening(
            onResult = { result ->
                handleUserVoiceQuery(result)
            },
            onError = { err ->
                voiceState = VoiceState.IDLE
                aiSpokenAnswer = err
            }
        )
    }

    // Auto-start listening on open
    LaunchedEffect(Unit) {
        startListening()
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.stopSpeaking()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            voiceManager.stopSpeaking()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Indicator Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when (voiceState) {
                            VoiceState.LISTENING -> Color(0xFFE3F2FD)
                            VoiceState.THINKING -> Color(0xFFFFF3E0)
                            VoiceState.SPEAKING -> Color(0xFFE8F5E9)
                            VoiceState.IDLE -> Color(0xFFF1F5F9)
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (voiceState) {
                                    VoiceState.LISTENING -> Color(0xFF007AFF)
                                    VoiceState.THINKING -> Color(0xFFFF9500)
                                    VoiceState.SPEAKING -> Color(0xFF34C759)
                                    VoiceState.IDLE -> SlateSubtitle
                                }
                            )
                    )
                    Text(
                        text = when (voiceState) {
                            VoiceState.LISTENING -> "LIVE LISTENING"
                            VoiceState.THINKING -> "AI THINKING"
                            VoiceState.SPEAKING -> "NUDGE SPEAKING"
                            VoiceState.IDLE -> "READY"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (voiceState) {
                            VoiceState.LISTENING -> Color(0xFF007AFF)
                            VoiceState.THINKING -> Color(0xFFE65100)
                            VoiceState.SPEAKING -> Color(0xFF2E7D32)
                            VoiceState.IDLE -> NavyText
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Pulsing Voice Orb & Animated Sound Rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Outer expanding ripple ring
                if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale2)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) Color(0x22007AFF)
                                else Color(0x2234C759)
                            )
                    )
                    // Inner expanding ripple ring
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .scale(pulseScale1)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) Color(0x33007AFF)
                                else Color(0x3334C759)
                            )
                    )
                }

                // Core Button Orb
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            when (voiceState) {
                                VoiceState.LISTENING -> Color(0xFF007AFF)
                                VoiceState.THINKING -> Color(0xFFFF9500)
                                VoiceState.SPEAKING -> Color(0xFF34C759)
                                VoiceState.IDLE -> Color(0xFF007AFF)
                            }
                        )
                        .clickable {
                            if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.IDLE) {
                                startListening()
                            } else if (voiceState == VoiceState.LISTENING) {
                                voiceManager.stopListening()
                                voiceState = VoiceState.IDLE
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (voiceState) {
                            VoiceState.LISTENING -> "🎙️"
                            VoiceState.THINKING -> "⏳"
                            VoiceState.SPEAKING -> "🔊"
                            VoiceState.IDLE -> "🎙️"
                        },
                        fontSize = 38.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Equalizer Waveform Bars (Active when listening or speaking)
            AnimatedAudioEqualizer(
                isActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Main Status Heading
            Text(
                text = when (voiceState) {
                    VoiceState.LISTENING -> "Listening to you..."
                    VoiceState.THINKING -> "Understanding..."
                    VoiceState.SPEAKING -> "Nudge's Advice"
                    VoiceState.IDLE -> "Tap to speak again"
                },
                color = NavyText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Subtitle text
            Text(
                text = when (voiceState) {
                    VoiceState.LISTENING -> "Speak naturally, for example:\n\"My phone has no sound\" or \"Internet isn't working\""
                    VoiceState.THINKING -> thinkingSteps[thinkingStepIndex]
                    VoiceState.SPEAKING -> "Listen to Nudge or read below:"
                    VoiceState.IDLE -> "Tap the blue button whenever you're ready to speak."
                },
                color = SlateSubtitle,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // User's speech transcript
            if (userSpokenQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You said: \"$userSpokenQuery\"",
                        color = NavyText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // AI Response Card
            if (aiSpokenAnswer.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F9FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nudge AI Solution:",
                                color = NudgeBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { voiceManager.speak(aiSpokenAnswer) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBF5FF)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "🔊 Replay",
                                    color = Color(0xFF007AFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiSpokenAnswer,
                            color = NavyText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )

                        resolutionBadge?.let { badge ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8F8F0))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "✓ $badge",
                                    color = Color(0xFF15803D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (executionSteps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚡ Autonomous Actions Performed:",
                                    color = NudgeBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                executionSteps.forEach { step ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "✓",
                                            color = Color(0xFF15803D),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = step.stepTitle,
                                                color = NavyText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = step.description,
                                                color = SlateSubtitle,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.IDLE) {
                    Button(
                        onClick = { startListening() },
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "🎙️ Speak Again",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = {
                        voiceManager.stopSpeaking()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Close",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AnimatedAudioEqualizer(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    @Composable
    fun animateBar(targetHeight: Float, duration: Int): Float {
        val height by infiniteTransition.animateFloat(
            initialValue = 6f,
            targetValue = if (isActive) targetHeight else 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "barHeight"
        )
        return height
    }

    val h1 = animateBar(24f, 320)
    val h2 = animateBar(34f, 410)
    val h3 = animateBar(42f, 290)
    val h4 = animateBar(30f, 370)
    val h5 = animateBar(22f, 450)

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(46.dp)
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { barHeight ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) Color(0xFF007AFF) else Color(0xFFCBD5E1))
            )
        }
    }
}