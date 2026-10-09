package com.tensormind.nudge.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.diagnostics.NetworkDiagnosticService
import com.tensormind.nudge.diagnostics.SpeedTestResult
import com.tensormind.nudge.diagnostics.SpeedTestStage
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.SlateSubtitle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkTroubleshooterSheet(
    networkService: NetworkDiagnosticService,
    onDismiss: () -> Unit,
    onSpokenFeedback: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var testStage by remember { mutableStateOf(SpeedTestStage.IDLE) }
    var currentSpeed by remember { mutableDoubleStateOf(0.0) }
    var currentPing by remember { mutableLongStateOf(0L) }
    var testResult by remember { mutableStateOf<SpeedTestResult?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    fun runFullDiagnostic() {
        isRunning = true
        testResult = null
        currentSpeed = 0.0
        currentPing = 0L
        scope.launch {
            val result = networkService.runConsumerSpeedTest { stage, speed, ping ->
                testStage = stage
                currentSpeed = speed
                if (ping > 0) currentPing = ping
            }
            testResult = result
            isRunning = false
            testStage = SpeedTestStage.COMPLETE
            onSpokenFeedback(result.plainEnglishVerdict)
        }
    }

    LaunchedEffect(Unit) {
        runFullDiagnostic()
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEBF5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Internet Speed & Health",
                        color = NavyText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Speedometer, Latency, DNS & Wi-Fi RSSI",
                        color = SlateSubtitle,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stage Status Banner
            val stageLabel = when (testStage) {
                SpeedTestStage.IDLE -> "Ready to test"
                SpeedTestStage.CHECKING_GATEWAY -> "1/4 Checking Wi-Fi gateway..."
                SpeedTestStage.MEASURING_DNS_AND_SIGNAL -> "2/4 Measuring DNS lookup & Wi-Fi signal..."
                SpeedTestStage.MEASURING_LATENCY -> "3/4 Measuring ping & jitter..."
                SpeedTestStage.MEASURING_DOWNLOAD -> "4/4 Testing download throughput..."
                SpeedTestStage.COMPLETE -> "Diagnostic Complete"
                SpeedTestStage.FAILED -> "Connection Test Failed"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (testStage == SpeedTestStage.COMPLETE) Color(0xFFE8F8F0) else Color(0xFFEBF5FF))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stageLabel,
                    color = if (testStage == SpeedTestStage.COMPLETE) Color(0xFF1E824C) else Color(0xFF007AFF),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Consumer-Grade Hero Speedometer Gauge
            SpeedometerGauge(
                speedMbps = if (testResult != null) testResult!!.downloadMbps else currentSpeed,
                maxSpeed = 100.0,
                isTesting = isRunning
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Primary 3 Metric Pills (Download, Latency, Jitter)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Download",
                    value = if (testResult != null) "${testResult!!.downloadMbps} Mbps" else if (currentSpeed > 0) "$currentSpeed Mbps" else "--",
                    icon = "⚡",
                    accent = Color(0xFF007AFF),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Latency",
                    value = if (testResult != null) "${testResult!!.pingMs} ms" else if (currentPing > 0) "$currentPing ms" else "--",
                    icon = "⏱️",
                    accent = Color(0xFF1E824C),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Jitter",
                    value = if (testResult != null) "${testResult!!.jitterMs} ms" else "--",
                    icon = "📶",
                    accent = Color(0xFFFF9F00),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary 3 Deep Network Metrics: DNS, Wi-Fi RSSI, MTU
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Advanced Network Signals",
                        color = NavyText,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. DNS Lookup Time
                    NetworkSignalRow(
                        icon = "🌐",
                        label = "DNS Lookup Time",
                        value = if (testResult != null) "${testResult!!.dnsLookupMs} ms" else "Testing...",
                        badge = if (testResult != null && testResult!!.dnsLookupMs < 50) "Ultra Fast" else "Normal",
                        isGood = (testResult?.dnsLookupMs ?: 0) < 80
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Wi-Fi Signal Strength (RSSI)
                    val rssiVal = testResult?.wifiRssi
                    val percentVal = testResult?.wifiSignalPercent
                    val rssiDisplay = if (rssiVal != null) "$rssiVal dBm ($percentVal%)" else "Cellular Data"
                    val rssiBadge = if (rssiVal != null && rssiVal > -65) "Strong Signal" else if (rssiVal != null) "Moderate" else "Active"

                    NetworkSignalRow(
                        icon = "📶",
                        label = "Wi-Fi Signal (RSSI)",
                        value = if (testResult != null) rssiDisplay else "Checking...",
                        badge = rssiBadge,
                        isGood = (rssiVal ?: -60) > -75
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Packet Size / MTU Behavior
                    val mtuVal = testResult?.mtuBytes ?: 1500
                    NetworkSignalRow(
                        icon = "📦",
                        label = "Packet MTU Size",
                        value = "$mtuVal bytes",
                        badge = if (mtuVal >= 1480) "Optimal (No Frag)" else "Standard",
                        isGood = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plain-English Senior Reassurance Card
            if (testResult != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (testResult!!.isConnected) Color(0xFFF4F9FF) else Color(0xFFFFF0F2)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Verdict: ${testResult!!.qualityRating}",
                                color = if (testResult!!.isConnected) Color(0xFF007AFF) else Color(0xFFD63031),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = testResult!!.plainEnglishVerdict,
                            color = NavyText,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Capabilities Checklist
                        CapabilityRow(label = "WhatsApp & Family Video Calls", isReady = testResult!!.canMakeVideoCalls)
                        Spacer(modifier = Modifier.height(6.dp))
                        CapabilityRow(label = "HD Streaming & Videos", isReady = testResult!!.canStreamVideo)
                        Spacer(modifier = Modifier.height(6.dp))
                        CapabilityRow(label = "Websites & Messaging", isReady = testResult!!.isConnected)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { runFullDiagnostic() },
                    enabled = !isRunning,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(text = "Test Speed Again", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        try {
                            context.startActivity(networkService.openWifiSettingsIntent())
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2EDFE))
                ) {
                    Text(text = "Wi-Fi Settings", color = NavyText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun SpeedometerGauge(
    speedMbps: Double,
    maxSpeed: Double = 100.0,
    isTesting: Boolean
) {
    val animatedSpeed = remember { Animatable(0f) }

    LaunchedEffect(speedMbps) {
        animatedSpeed.animateTo(
            targetValue = speedMbps.toFloat(),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(210.dp)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 16.dp.toPx()
            val startAngle = 140f
            val sweepAngle = 260f

            // Background Track Arc
            drawArc(
                color = Color(0xFFE2EDFE),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )

            // Progress Arc with Gradient
            val progressRatio = (animatedSpeed.value / maxSpeed.toFloat()).coerceIn(0f, 1f)
            val currentSweep = sweepAngle * progressRatio

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFF007AFF), Color(0xFF00C6FF), Color(0xFF00E5FF))
                ),
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }

        // Center Digital Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", animatedSpeed.value),
                color = NavyText,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Mbps Download",
                color = SlateSubtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = SlateSubtitle, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = NavyText, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NetworkSignalRow(
    icon: String,
    label: String,
    value: String,
    badge: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = SlateSubtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = NavyText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isGood) Color(0xFFE8F8F0) else Color(0xFFFFFBEA))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badge,
                color = if (isGood) Color(0xFF1E824C) else Color(0xFFB7791F),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CapabilityRow(label: String, isReady: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isReady) "✅" else "⚠️",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = if (isReady) NavyText else Color(0xFF888888),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (isReady) "Smooth" else "Slow",
            color = if (isReady) Color(0xFF1E824C) else Color(0xFFFF9F00),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}