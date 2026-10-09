package com.tensormind.nudge.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.diagnostics.AudioDiagnosticService
import com.tensormind.nudge.diagnostics.AudioStatus
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundTroubleshooterSheet(
    audioService: AudioDiagnosticService,
    onDismiss: () -> Unit,
    onSpokenFeedback: (String) -> Unit
) {
    var status by remember { mutableStateOf(audioService.inspectAudio()) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF0F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Sound Troubleshooter",
                        color = NavyText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "We check why your phone is silent.",
                        color = SlateSubtitle,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Diagnostic status card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (status.issuesFound.isEmpty()) Color(0xFFE8F8F0) else Color(0xFFFFF0F2)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (status.issuesFound.isEmpty()) "All Sound Working Great" else "Issue Found",
                        color = if (status.issuesFound.isEmpty()) Color(0xFF1E824C) else Color(0xFFD63031),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status.summary,
                        color = NavyText,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live status meters
            VolumeMeterRow(label = "Ringtone Volume", percent = status.ringerVolumePercent)
            Spacer(modifier = Modifier.height(10.dp))
            VolumeMeterRow(label = "Media / Video Volume", percent = status.mediaVolumePercent)

            Spacer(modifier = Modifier.height(14.dp))

            if (status.isSilentOrVibrate) {
                StatusTag(text = "⚠️ Silent / Vibrate Mode is Active", isWarning = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (status.isDndActive) {
                StatusTag(text = "⚠️ Do Not Disturb is Blocking Notifications", isWarning = true)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (actionMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = actionMessage!!,
                    color = Color(0xFF1E824C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fix button
            Button(
                onClick = {
                    val result = audioService.fixSoundIssues()
                    actionMessage = result
                    status = audioService.inspectAudio()
                    onSpokenFeedback(result)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue)
            ) {
                Text(
                    text = "Fix All Sound Issues Now",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun VolumeMeterRow(label: String, percent: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = NavyText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "$percent%", color = SlateSubtitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE2EDFE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (percent == 0) Color(0xFFFF4D6D) else Color(0xFF007AFF))
            )
        }
    }
}

@Composable
fun StatusTag(text: String, isWarning: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isWarning) Color(0xFFFFFBEA) else Color(0xFFE8F8F0))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isWarning) Color(0xFFB7791F) else Color(0xFF1E824C),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
