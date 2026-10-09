package com.tensormind.nudge.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.tensormind.nudge.diagnostics.ActivityLogEntry
import com.tensormind.nudge.diagnostics.NudgeActivityRepository
import com.tensormind.nudge.ui.components.NudgeBottomNavigation
import com.tensormind.nudge.ui.theme.BgGradientEnd
import com.tensormind.nudge.ui.theme.BgGradientMid
import com.tensormind.nudge.ui.theme.BgGradientStart
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeTheme
import com.tensormind.nudge.ui.theme.SlateSubtitle

@Composable
fun HistoryScreen(
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val activities = NudgeActivityRepository.activities
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, FIX, SCAM, CHECKUP
    var selectedItemForDetail by remember { mutableStateOf<ActivityLogEntry?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredList = when (selectedFilter) {
        "FIX" -> activities.filter { it.category == "FIX" || it.category == "BOOST" }
        "SCAM" -> activities.filter { it.category == "SCAM" }
        "CHECKUP" -> activities.filter { it.category == "CHECKUP" }
        else -> activities
    }

    val fixCount = activities.count { it.category == "FIX" || it.category == "BOOST" }
    val scamCount = activities.count { it.category == "SCAM" }

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

                // 2. Summary Statistics Metric Card (Dynamic)
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
                                text = "$fixCount Issues Fixed",
                                color = Color(0xFF21A15C),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automated phone restorations",
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
                                text = "$scamCount Scams Caught",
                                color = Color(0xFF0E73F6),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Phishing threats blocked",
                                color = SlateSubtitle,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${activities.size})", fontSize = 12.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0E73F6),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "FIX",
                        onClick = { selectedFilter = "FIX" },
                        label = { Text("Fixes ($fixCount)", fontSize = 12.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF21A15C),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "SCAM",
                        onClick = { selectedFilter = "SCAM" },
                        label = { Text("Scams ($scamCount)", fontSize = 12.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE53935),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Activity List
                if (filteredList.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No activities found in this filter",
                                color = SlateSubtitle,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    NudgeActivityRepository.populateDefaults()
                                    selectedFilter = "ALL"
                                    Toast.makeText(context, "Default log restored", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E73F6)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Restore Activity Records")
                            }
                        }
                    }
                } else {
                    filteredList.forEach { item ->
                        HistoryCard(
                            item = item,
                            onClick = { selectedItemForDetail = item }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Actions Row (Clear / Restore)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (activities.isNotEmpty()) {
                                showClearConfirmDialog = true
                            } else {
                                Toast.makeText(context, "Log is already empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Clear Log",
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            NudgeActivityRepository.populateDefaults()
                            selectedFilter = "ALL"
                            Toast.makeText(context, "Sample log loaded", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Reset Defaults",
                            color = NavyText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Detail Dialog
    selectedItemForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItemForDetail = null },
            title = {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = NavyText
                )
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .background(item.badgeBgColor, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${item.badgeText} • ${item.timestamp}",
                            color = item.badgeTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.description,
                        color = NavyText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedItemForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E73F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Understood")
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All History?", color = NavyText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear your phone checkup log?") },
            confirmButton = {
                Button(
                    onClick = {
                        NudgeActivityRepository.clearAll()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = SlateSubtitle)
                }
            }
        )
    }
}

@Composable
private fun HistoryCard(
    item: ActivityLogEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }

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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .background(item.badgeBgColor, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.badgeText,
                            color = item.badgeTextColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    color = SlateSubtitle,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.timestamp,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 840)
@Composable
fun HistoryScreenPreview() {
    NudgeTheme {
        HistoryScreen()
    }
}
