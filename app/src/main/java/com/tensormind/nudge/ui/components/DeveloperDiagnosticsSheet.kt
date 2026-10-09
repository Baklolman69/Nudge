package com.tensormind.nudge.ui.components

import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.ai.AgentTelemetry
import com.tensormind.nudge.ai.GroqQwenClient
import com.tensormind.nudge.diagnostics.AudioDiagnosticService
import com.tensormind.nudge.diagnostics.DeviceControlService
import com.tensormind.nudge.diagnostics.DeviceHealthService
import com.tensormind.nudge.diagnostics.NetworkDiagnosticService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

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
    val scope = rememberCoroutineScope()

    var consoleOutput by remember { mutableStateOf("System Diagnostics Initialized. Standby for commands...") }
    var pingLatency by remember { mutableStateOf<String?>("Checking...") }
    var torchStatus by remember { mutableStateOf(if (deviceControl.isTorchActive()) "ON" else "OFF") }

    // Read system metrics
    val runtime = Runtime.getRuntime()
    val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMemMb = runtime.maxMemory() / (1024 * 1024)

    // Battery metrics
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val bStatus = context.registerReceiver(null, ifilter)
    val tempC = (bStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0f
    val bLevel = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0

    // Storage
    val stat = StatFs(Environment.getDataDirectory().path)
    val freeGb = (stat.availableBlocksLong * stat.blockSizeLong).toDouble() / (1024 * 1024 * 1024)
    val totalGb = (stat.blockCountLong * stat.blockSizeLong).toDouble() / (1024 * 1024 * 1024)

    val audioInfo = audioService.inspectAudio()
    val netInfo = networkService.inspectNetwork()
    val telemetry = groqClient.latestTelemetry

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val latency = try {
                val start = System.currentTimeMillis()
                val reachable = InetAddress.getByName("8.8.8.8").isReachable(1500)
                val duration = System.currentTimeMillis() - start
                if (reachable) "${duration}ms" else "Timeout"
            } catch (_: Exception) {
                "Offline"
            }
            withContext(Dispatchers.Main) {
                pingLatency = latency
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A), // Dark Slate Developer Terminal
        contentColor = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Terminal Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AGENT CONSOLE & TELEMETRY",
                        color = Color(0xFF22C55E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "API ${Build.VERSION.SDK_INT} | DEBUG",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Autonomous Mode Toggle Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Autonomous Action Execution",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (autonomousMode) "AI executes system tools automatically upon user query" else "AI asks for confirmation before executing actions",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = autonomousMode,
                        onCheckedChange = onAutonomousModeChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Hardware & Telemetry Grid
            Text(
                text = "SYSTEM TELEMETRY",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    TelemetryRow("RAM Usage", "${usedMemMb}MB / ${maxMemMb}MB limit")
                    TelemetryRow("Battery Temp", "${tempC}°C (Level: ${bLevel}%)")
                    TelemetryRow("Storage Free", "${String.format("%.1f", freeGb)}GB / ${String.format("%.1f", totalGb)}GB")
                    TelemetryRow("Ping Latency (8.8.8.8)", pingLatency ?: "Measuring...")
                    TelemetryRow("Audio Ringer Stream", "Ringer Vol: ${audioInfo.ringerVolumePercent}% (Silent/Vibe: ${audioInfo.isSilentOrVibrate})")
                    TelemetryRow("DND Filter Status", if (audioInfo.isDndActive) "ACTIVE (INTERRUPTING)" else "INACTIVE (OK)")
                    TelemetryRow("Active Network", if (netInfo.isConnected) "${if (netInfo.isWifi) "Wi-Fi" else "Cellular"} (ONLINE)" else "DISCONNECTED")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Live LLM Tool-Call Stream
            Text(
                text = "LATEST AGENT INFERENCE & TOOL CALL",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Model: ${telemetry?.modelUsed ?: "Idle"}",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${telemetry?.latencyMs ?: 0}ms",
                            color = Color(0xFFFBBF24),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Dispatched Tool Action: ${telemetry?.actionCommand ?: "NONE"}",
                        color = Color(0xFF4ADE80),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "RAW JSON PAYLOAD:",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111827))
                            .padding(10.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = telemetry?.rawJson ?: "{\n  \"status\": \"idle\",\n  \"waiting\": \"Speak or query Nudge to observe live tool execution\"\n}",
                            color = Color(0xFFA5B4FC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Direct Command Execution Palette
            Text(
                text = "DIRECT COMMAND PALETTE",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val res = deviceControl.toggleTorch()
                        torchStatus = if (deviceControl.isTorchActive()) "ON" else "OFF"
                        consoleOutput = "CMD: toggleTorch() -> $res"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Torch: $torchStatus", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = {
                        val res = audioService.fixSoundIssues()
                        consoleOutput = "CMD: fixSoundIssues() -> $res"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Fix Audio 85%", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        deviceControl.openDisplaySettings()
                        consoleOutput = "CMD: openDisplaySettings() -> Launched Intent"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Display Intent", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = {
                        deviceControl.openAlarmSettings()
                        consoleOutput = "CMD: openAlarmSettings() -> Launched Intent"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Alarm Intent", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Console output feedback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(10.dp)
            ) {
                Text(
                    text = "> $consoleOutput",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Color(0xFFF1F5F9),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
