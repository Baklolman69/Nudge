package com.tensormind.nudge.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.R
import com.tensormind.nudge.ai.GroqQwenClient
import com.tensormind.nudge.diagnostics.AudioDiagnosticService
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle
import com.tensormind.nudge.voice.NudgeVoiceManager
import kotlinx.coroutines.launch

import android.content.Intent
import android.provider.Settings
import com.tensormind.nudge.diagnostics.DeviceControlService

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantDialog(
    voiceManager: NudgeVoiceManager,
    groqClient: GroqQwenClient,
    audioService: AudioDiagnosticService,
    deviceControl: DeviceControlService? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val devControl = deviceControl ?: remember { DeviceControlService(context) }
    var queryText by remember { mutableStateOf("") }
    var typedInput by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("How can I help you today? Speak or type below.") }
    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    fun handleUserQuery(query: String) {
        if (query.isBlank()) return
        queryText = query
        typedInput = ""
        isListening = false
        isThinking = true
        responseText = "Consulting Qwen 2.5 AI..."
        scope.launch {
            val agentResp = groqClient.processUserVoiceQuery(query)
            var reply = agentResp.replyText

            when (agentResp.actionCommand) {
                "FIX_AUDIO" -> {
                    val fixResult = audioService.fixSoundIssues()
                    reply = "$reply $fixResult"
                }
                "TOGGLE_TORCH" -> {
                    val torchRes = devControl.toggleTorch()
                    reply = "$reply $torchRes"
                }
                "OPEN_BRIGHTNESS" -> {
                    devControl.openDisplaySettings()
                    reply = "$reply Opening display settings."
                }
                "OPEN_ALARM" -> {
                    devControl.openAlarmSettings()
                    reply = "$reply Opening clock and alarms."
                }
                "CALL_FAMILY" -> {
                    devControl.openDialer()
                    reply = "$reply Opening phone dialer."
                }
                "OPEN_SETTINGS" -> {
                    devControl.openApplicationSettings()
                    reply = "$reply Opening settings."
                }
                "OPEN_WIFI" -> {
                    try {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    } catch (_: Exception) {}
                }
                "OPEN_BLUETOOTH" -> {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    } catch (_: Exception) {}
                }
                "OPEN_BATTERY" -> {
                    try {
                        context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    } catch (_: Exception) {}
                }
            }

            responseText = reply
            isThinking = false
            voiceManager.speak(reply)
        }
    }

    fun startListeningFlow() {
        voiceManager.stopSpeaking()
        isListening = true
        isThinking = false
        responseText = "Listening... Speak naturally."
        voiceManager.startListening(
            onResult = { speech ->
                handleUserQuery(speech)
            },
            onError = { err ->
                isListening = false
                isThinking = false
                responseText = err
            }
        )
    }

    LaunchedEffect(Unit) {
        startListeningFlow()
    }

    ModalBottomSheet(
        onDismissRequest = {
            voiceManager.stopSpeaking()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Glowing Mic / Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(86.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(if (isListening) Color(0xFFD9E9FF) else Color(0xFFE8F8F0))
                    .clickable {
                        if (!isListening) startListeningFlow()
                    }
            ) {
                Text(
                    text = if (isListening) "🎙️" else if (isThinking) "⏳" else "🤖",
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isListening) "I'm Listening..." else if (isThinking) "Thinking with Qwen 2.5..." else "Nudge Assistant",
                color = NavyText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (queryText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "You asked: \"$queryText\"",
                    color = SlateSubtitle,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Response Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F9FE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nudge AI:",
                            color = NudgeBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isThinking) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = NudgeBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = responseText,
                        color = NavyText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Suggestion Chips
            Text(
                text = "Or tap a common question:",
                color = SlateSubtitle,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "🔊 Phone has no sound",
                    "🌐 Internet is slow",
                    "🔋 Check my battery health",
                    "📱 Where is my photo?"
                ).forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEBF5FF))
                            .clickable {
                                handleUserQuery(suggestion.substring(2).trim())
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = suggestion,
                            color = Color(0xFF007AFF),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dual Input Bar: Type text + Voice Mic button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedInput,
                    onValueChange = { typedInput = it },
                    placeholder = { Text("Type any phone issue...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        handleUserQuery(typedInput)
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send typed query button
                if (typedInput.isNotBlank()) {
                    Button(
                        onClick = { handleUserQuery(typedInput) },
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue)
                    ) {
                        Text(text = "➤", fontSize = 18.sp, color = Color.White)
                    }
                } else {
                    // Tap to speak mic button
                    Button(
                        onClick = { startListeningFlow() },
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) Color(0xFFFF4D6D) else NudgeBlue
                        )
                    ) {
                        Text(text = "🎙️", fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Close button
            Button(
                onClick = {
                    voiceManager.stopSpeaking()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F4F8))
            ) {
                Text(text = "Done", color = NavyText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}