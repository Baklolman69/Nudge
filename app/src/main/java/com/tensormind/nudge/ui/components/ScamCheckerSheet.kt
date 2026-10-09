package com.tensormind.nudge.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.ai.GroqQwenClient
import com.tensormind.nudge.ai.RiskLevel
import com.tensormind.nudge.ai.ScamAnalysisResult
import com.tensormind.nudge.diagnostics.SmsDiagnosticService
import com.tensormind.nudge.diagnostics.SmsMessageItem
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle
import com.tensormind.nudge.R
import com.tensormind.nudge.diagnostics.NudgeActivityRepository
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScamCheckerSheet(
    groqClient: GroqQwenClient,
    smsService: SmsDiagnosticService,
    initialText: String = "",
    onDismiss: () -> Unit,
    onSpokenFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val recentMessages = remember { smsService.getRecentMessages() }
    var selectedMessage by remember { mutableStateOf<SmsMessageItem?>(null) }
    var customMessageText by remember { mutableStateOf(initialText) }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ScamAnalysisResult?>(null) }

    fun analyze(textToAnalyze: String) {
        if (textToAnalyze.isBlank()) return
        isLoading = true
        result = null
        scope.launch {
            val res = groqClient.analyzeScamMessage(textToAnalyze)
            result = res
            isLoading = false
            onSpokenFeedback(res.headline + ". " + res.recommendedAction)

            // Log activity to History
            val isSafe = res.risk == RiskLevel.SAFE
            NudgeActivityRepository.logActivity(
                title = if (isSafe) "Message Checked (Safe)" else "Phishing SMS Evaluated",
                description = "${res.headline}: ${res.explanation}",
                category = "SCAM",
                badgeText = if (isSafe) "Safe Message" else "Scam Blocked",
                badgeBg = if (isSafe) Color(0xFFE8F8F0) else Color(0xFFFFEEEE),
                badgeTextCol = if (isSafe) Color(0xFF1E824C) else Color(0xFFE53935),
                iconRes = R.drawable.ic_shield_purple
            )
        }
    }

    LaunchedEffect(initialText) {
        if (initialText.isNotBlank()) {
            customMessageText = initialText
            analyze(initialText)
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFBEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛡️", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Scam & Safety Shield",
                        color = NavyText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any message to verify with Qwen 2.5 AI",
                        color = SlateSubtitle,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Recent SMS Picker (1-Tap Selection)
            Text(
                text = "📩 Your Recent Messages (Tap to Scan):",
                color = NavyText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            recentMessages.forEach { msg ->
                val isSelected = selectedMessage?.id == msg.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEBF5FF) else Color(0xFFF8FAFD)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NudgeBlue else Color(0xFFE5ECF6),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedMessage = msg
                            customMessageText = msg.body
                            analyze(msg.body)
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg.sender,
                                color = NavyText,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = msg.dateString,
                                color = SlateSubtitle,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = msg.body,
                            color = SlateSubtitle,
                            fontSize = 13.sp,
                            maxLines = 2,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (msg.isSuspiciousHint) {
                                Text(
                                    text = "⚠️ Urgent / Link detected",
                                    color = Color(0xFFB7791F),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "👉 Tap to Scan",
                                color = NudgeBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = NudgeBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyzing message with Qwen 2.5 AI...",
                            color = NavyText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Results Display Card
            if (result != null) {
                Spacer(modifier = Modifier.height(12.dp))

                val (badgeBg, badgeText, badgeColor) = when (result!!.risk) {
                    RiskLevel.DANGEROUS -> Triple(Color(0xFFFFF0F2), "⚠️ DANGEROUS SCAM DETECTED", Color(0xFFD63031))
                    RiskLevel.SUSPICIOUS -> Triple(Color(0xFFFFFBEA), "⚠️ CAUTION RECOMMENDED", Color(0xFFB7791F))
                    RiskLevel.SAFE -> Triple(Color(0xFFE8F8F0), "✅ APPEARS SAFE", Color(0xFF1E824C))
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = badgeBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result!!.headline,
                            color = NavyText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result!!.explanation,
                            color = NavyText,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "👉 What to do: ${result!!.recommendedAction}",
                            color = badgeColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Feature 1: Family Warning Share Button
                        if (result!!.risk == RiskLevel.DANGEROUS) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Hi! Nudge warned me that a text from '${selectedMessage?.sender ?: "an unknown number"}' is a dangerous scam. I did not click the link. Just letting you know!"
                                            )
                                        }
                                        val chooser = Intent.createChooser(shareIntent, "Share Warning with Family").apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(chooser)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))
                            ) {
                                Text(
                                    text = "Send Warning to Family",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 2: Custom Text Field (Paste / Type alternative)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Or type / paste any other text:",
                color = SlateSubtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customMessageText,
                onValueChange = { customMessageText = it },
                placeholder = { Text("Paste message here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { analyze(customMessageText) },
                enabled = customMessageText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue)
            ) {
                Text(
                    text = "Analyze Custom Text",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}