package com.tensormind.nudge.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tensormind.nudge.ai.NudgeSkillsEngine
import com.tensormind.nudge.ai.SkillExecutionStep
import com.tensormind.nudge.diagnostics.DeviceControlService
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class AgentActionDetail(
    val title: String,
    val description: String,
    val icon: String,
    val buttonText: String,
    val actionType: String
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val steps: List<SkillExecutionStep> = emptyList(),
    val badgeTitle: String? = null,
    val badgeColor: Color = Color.Transparent,
    val badgeTextColor: Color = Color.Transparent,
    val actionDetail: AgentActionDetail? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatAssistantDialog(
    groqClient: GroqQwenClient,
    audioService: AudioDiagnosticService,
    deviceControl: DeviceControlService? = null,
    skillsEngine: NudgeSkillsEngine? = null,
    onOpenScamSheet: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val devControl = deviceControl ?: remember { DeviceControlService(context) }
    val engine = skillsEngine ?: remember { NudgeSkillsEngine(context, audioService, devControl) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = false,
                text = "Hello! I'm Nudge, your autonomous phone companion. I can run commands and fix settings on your phone for you—just ask me in simple words!"
            )
        )
    }

    var typedInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var agentProgress by remember { mutableFloatStateOf(0.25f) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val agentSteps = listOf(
        Pair("\uD83E\uDD14 Thinking...", "Understanding what happened to your phone"),
        Pair("\u2699\uFE0F Fixing settings...", "Running diagnostic check on your device"),
        Pair("\uD83D\uDD27 Taking action...", "Executing fix on your phone settings"),
        Pair("\u2728 Just final step...", "Preparing simple, clear confirmation for you")
    )

    LaunchedEffect(isThinking) {
        if (isThinking) {
            currentStepIndex = 0
            agentProgress = 0.25f
            val stepDelays = listOf(800L, 1000L, 900L, 700L)
            for (i in agentSteps.indices) {
                currentStepIndex = i
                agentProgress = (i + 1).toFloat() / agentSteps.size.toFloat()
                delay(stepDelays[i])
                if (!isThinking) break
            }
        }
    }

    fun executePhoneAction(actionType: String) {
        when (actionType) {
            "FIX_AUDIO" -> {
                audioService.fixSoundIssues(playChime = false)
                Toast.makeText(context, "Volume restored to 85%! Silent mode deactivated.", Toast.LENGTH_SHORT).show()
            }
            "OPEN_WIFI" -> {
                try {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Opening Wi-Fi settings...", Toast.LENGTH_SHORT).show()
                }
            }
            "OPEN_BLUETOOTH" -> {
                try {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Opening Bluetooth settings...", Toast.LENGTH_SHORT).show()
                }
            }
            "OPEN_BATTERY" -> {
                try {
                    val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Opening Battery Saver...", Toast.LENGTH_SHORT).show()
                }
            }
            "CALL_FAMILY" -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Opening Phone dialer...", Toast.LENGTH_SHORT).show()
                }
            }
            "OPEN_SCAM_CHECK" -> {
                onOpenScamSheet()
            }
        }
    }

    fun sendMessage(query: String) {
        if (query.isBlank() || isThinking) return
        val trimmed = query.trim()
        typedInput = ""
        messages.add(ChatMessage(isUser = true, text = trimmed))
        isThinking = true

        scope.launch {
            delay(100)
            listState.animateScrollToItem(messages.size)

            val groqResp = groqClient.processUserVoiceQuery(trimmed)
            val execution = engine.executeAutonomousWorkflow(trimmed)
            isThinking = false

            val replyText = if (groqResp.replyText.isNotBlank()) groqResp.replyText else execution.finalResolutionReport
            messages.add(
                ChatMessage(
                    isUser = false,
                    text = replyText,
                    steps = execution.steps,
                    badgeTitle = execution.badgeTitle,
                    badgeColor = execution.badgeColor,
                    badgeTextColor = execution.badgeTextColor
                )
            )
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEBF5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_nudge),
                        contentDescription = "Nudge Chat Mascot",
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chat with Nudge AI Agent",
                        color = NavyText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Autonomous phone fixer & assistant",
                        color = Color(0xFF007AFF),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Text(text = "\u2715", fontSize = 18.sp, color = SlateSubtitle)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "⚡ Boost my phone speed",
                    "🔊 Fix my sound",
                    "🌐 Check internet connection",
                    "🔋 Battery & storage check",
                    "🛡️ Check suspicious text",
                    "🔦 Flashlight",
                    "📞 Call my family"
                ).forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable {
                                sendMessage(suggestion.substring(2).trim())
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = suggestion,
                            color = NavyText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(310.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = NudgeBlue),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEBF5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🤖", fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F9FE)),
                                modifier = Modifier.fillMaxWidth(0.92f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Nudge AI Agent",
                                        color = NudgeBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.text,
                                        color = NavyText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 21.sp
                                    )

                                    // Badge showing action result
                                    if (msg.badgeTitle != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(msg.badgeColor)
                                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "✓ ${msg.badgeTitle}",
                                                color = msg.badgeTextColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Executed skills steps list
                                    if (msg.steps.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFFE2EDFE), RoundedCornerShape(12.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "⚡ Autonomous Actions Performed:",
                                                    color = NudgeBlue,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                msg.steps.forEach { step ->
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

                                    if (msg.actionDetail != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFFCCE4FF), RoundedCornerShape(14.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(text = msg.actionDetail.icon, fontSize = 20.sp)
                                                    Column {
                                                        Text(
                                                            text = "⚡ Action Executed by Agent",
                                                            color = Color(0xFF007AFF),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = msg.actionDetail.title,
                                                            color = NavyText,
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = msg.actionDetail.description,
                                                    color = SlateSubtitle,
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = { executePhoneAction(msg.actionDetail.actionType) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBF5FF)),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = msg.actionDetail.buttonText,
                                                        color = Color(0xFF007AFF),
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isThinking) {
                    item {
                        AnimatedVisibility(
                            visible = isThinking,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEBF5FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "\u2728", fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F6FD)),
                                    modifier = Modifier.fillMaxWidth(0.92f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                AnimatedJumpingDots()
                                                Spacer(modifier = Modifier.width(4.dp))
                                                AnimatedContent(
                                                    targetState = agentSteps[currentStepIndex].first,
                                                    transitionSpec = {
                                                        fadeIn(animationSpec = tween(300)) togetherWith
                                                            fadeOut(animationSpec = tween(200))
                                                    },
                                                    label = "stepTitle"
                                                ) { title ->
                                                    Text(
                                                        text = title,
                                                        color = NudgeBlue,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${currentStepIndex + 1}/${agentSteps.size}",
                                                color = SlateSubtitle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        AnimatedContent(
                                            targetState = agentSteps[currentStepIndex].second,
                                            transitionSpec = {
                                                fadeIn(animationSpec = tween(300)) togetherWith
                                                    fadeOut(animationSpec = tween(200))
                                            },
                                            label = "stepSub"
                                        ) { desc ->
                                            Text(
                                                text = desc,
                                                color = SlateSubtitle,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { agentProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = NudgeBlue,
                                            trackColor = Color(0xFFD9E9FF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedInput,
                    onValueChange = { typedInput = it },
                    placeholder = { Text("Ask anything about your phone...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        sendMessage(typedInput)
                    })
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { sendMessage(typedInput) },
                    enabled = typedInput.isNotBlank() && !isThinking,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (typedInput.isNotBlank()) NudgeBlue else Color(0xFFC7D7EC)
                    )
                ) {
                    Text(text = "\u27A4", fontSize = 20.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnimatedJumpingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "jumpingDots")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .offset(y = offset1.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(NudgeBlue)
        )
        Box(
            modifier = Modifier
                .offset(y = offset2.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(NudgeBlue)
        )
        Box(
            modifier = Modifier
                .offset(y = offset3.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(NudgeBlue)
        )
    }
}
