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
import com.tensormind.nudge.diagnostics.DeviceHealthReport
import com.tensormind.nudge.diagnostics.DeviceHealthService
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullCheckupSheet(
    healthService: DeviceHealthService,
    onDismiss: () -> Unit
) {
    var report by remember { mutableStateOf(healthService.runFullCheckup()) }
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
                        .background(Color(0xFFE8F8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🩺", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Full Phone Health Scan",
                        color = NavyText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Overall Status: ${report.overallScore}",
                        color = if (report.overallScore == "Excellent") Color(0xFF1E824C) else Color(0xFFB7791F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HealthItemCard(
                title = "Battery Level",
                value = "${report.batteryPercent}% " + if (report.isCharging) "(Charging)" else "",
                isHealthy = report.batteryPercent > 20,
                icon = "🔋"
            )

            Spacer(modifier = Modifier.height(10.dp))

            HealthItemCard(
                title = "Available Storage Space",
                value = "${report.freeStorageGb} GB Free of ${report.totalStorageGb} GB",
                isHealthy = report.freeStorageGb > 2.0,
                icon = "💾"
            )

            Spacer(modifier = Modifier.height(10.dp))

            HealthItemCard(
                title = "Phone Ringer & Volume",
                value = if (report.soundHealthy) "All Volumes Healthy" else "Muted or Silent Mode Active",
                isHealthy = report.soundHealthy,
                icon = "🔊"
            )

            Spacer(modifier = Modifier.height(10.dp))

            HealthItemCard(
                title = "Internet & Network",
                value = if (report.networkHealthy) "Connected to Internet" else "Disconnected",
                isHealthy = report.networkHealthy,
                icon = "🌐"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    report = healthService.runFullCheckup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue)
            ) {
                Text(
                    text = "Re-Scan Device",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HealthItemCard(
    title: String,
    value: String,
    isHealthy: Boolean,
    icon: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = NavyText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = value, color = SlateSubtitle, fontSize = 13.sp)
            }
            Text(
                text = if (isHealthy) "✅ Healthy" else "⚠️ Check",
                color = if (isHealthy) Color(0xFF1E824C) else Color(0xFFFF9F00),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
