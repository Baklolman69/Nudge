package com.tensormind.nudge.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tensormind.nudge.ui.components.NudgeBottomNavigation
import com.tensormind.nudge.ui.theme.BgGradientEnd
import com.tensormind.nudge.ui.theme.BgGradientMid
import com.tensormind.nudge.ui.theme.BgGradientStart
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeTheme
import com.tensormind.nudge.ui.theme.SlateSubtitle

data class HistoryItemData(
    val title: String,
    val description: String,
    val timeAgo: String,
    val badgeText: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val iconRes: Int
)

@Composable
fun HistoryScreen(
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val historyItems = rememberHistoryItems()

    Scaffold(
        bottomBar = {
            NudgeBottomNavigation(
                selectedTab = 1,
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
                // 1. Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_nudge),
                        contentDescription = "Mascot Logo",
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Activity & Fix History",
                            color = NavyText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Everything Nudge checked and fixed for you",
                            color = SlateSubtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Summary Statistics Metric Card
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
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "3 Issues Fixed",
                                color = Color(0xFF21A15C),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Today's automated fixes",
                                color = SlateSubtitle,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0xFFE2E8F0))
                        )

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "0 Scams Detected",
                                color = Color(0xFF0E73F6),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your phone is 100% safe",
                                color = SlateSubtitle,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Section Title
                Text(
                    text = "Recent Activity Log",
                    color = NavyText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Timeline History Items
                historyItems.forEach { item ->
                    HistoryCard(item = item)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Clear Log Button
                Button(
                    onClick = {
                        Toast.makeText(context, "History log cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F2FF))
                ) {
                    Text(
                        text = "Clear History Log",
                        color = Color(0xFF0E73F6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = NavyText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                color = item.badgeBgColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.badgeText,
                            color = item.badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.description,
                    color = SlateSubtitle,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.timeAgo,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun rememberHistoryItems(): List<HistoryItemData> {
    return listOf(
        HistoryItemData(
            title = "Volume & Sound Restored",
            description = "Ringer volume set to 85% and Do Not Disturb turned off.",
            timeAgo = "Today, 10:15 AM",
            badgeText = "Fixed",
            badgeBgColor = Color(0xFFE8F8F0),
            badgeTextColor = Color(0xFF21A15C),
            iconRes = R.drawable.ic_volume_red
        ),
        HistoryItemData(
            title = "Wi-Fi Network Reconnected",
            description = "Connected to Home_WiFi_5G automatically.",
            timeAgo = "Yesterday, 4:30 PM",
            badgeText = "Connected",
            badgeBgColor = Color(0xFFEBF5FF),
            badgeTextColor = Color(0xFF007AFF),
            iconRes = R.drawable.ic_wifi_blue
        ),
        HistoryItemData(
            title = "SMS Scam Scan Completed",
            description = "Scanned incoming message from +1-800-BANK-ALERT: Clean & Safe.",
            timeAgo = "Yesterday, 2:10 PM",
            badgeText = "Safe",
            badgeBgColor = Color(0xFFFFFBEA),
            badgeTextColor = Color(0xFFFF9F00),
            iconRes = R.drawable.ic_shield_yellow
        ),
        HistoryItemData(
            title = "Full Device Checkup",
            description = "Cleaned 1.2 GB cache files and checked battery health.",
            timeAgo = "Oct 6, 11:00 AM",
            badgeText = "Optimized",
            badgeBgColor = Color(0xFFF3EFFF),
            badgeTextColor = Color(0xFF6C5CE7),
            iconRes = R.drawable.ic_phone_purple
        ),
        HistoryItemData(
            title = "Family Call Completed",
            description = "Called Daughter (Sarah) via quick launcher.",
            timeAgo = "Oct 5, 6:00 PM",
            badgeText = "Completed",
            badgeBgColor = Color(0xFFE8F8F0),
            badgeTextColor = Color(0xFF21A15C),
            iconRes = R.drawable.ic_people_green
        )
    )
}

@Preview(showBackground = true, widthDp = 380, heightDp = 840)
@Composable
fun HistoryScreenPreview() {
    NudgeTheme {
        HistoryScreen()
    }
}
