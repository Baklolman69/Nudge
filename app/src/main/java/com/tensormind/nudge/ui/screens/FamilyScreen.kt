package com.tensormind.nudge.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tensormind.nudge.diagnostics.FamilyContact
import com.tensormind.nudge.diagnostics.FamilyRepository
import com.tensormind.nudge.ui.components.NudgeBottomNavigation
import com.tensormind.nudge.ui.theme.BgGradientEnd
import com.tensormind.nudge.ui.theme.BgGradientMid
import com.tensormind.nudge.ui.theme.BgGradientStart
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeTheme
import com.tensormind.nudge.ui.theme.SlateSubtitle

@Composable
fun FamilyScreen(
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var autoAlertsEnabled by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemberForOptions by remember { mutableStateOf<FamilyContact?>(null) }

    val familyMembers = FamilyRepository.familyMembers
    val primaryMember = FamilyRepository.getPrimaryMember()

    Scaffold(
        bottomBar = {
            NudgeBottomNavigation(
                selectedTab = 2,
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
                            text = "Family & Caregivers",
                            color = NavyText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Get help from your trusted loved ones anytime",
                            color = SlateSubtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Prominent Primary Caregiver Call Button
                if (primaryMember != null) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${primaryMember.phoneNumber}"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Calling ${primaryMember.name}...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21A15C))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_people_green),
                                contentDescription = null,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Call Primary Caregiver",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFE8F8F0),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${primaryMember.name} (${primaryMember.relationship})",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 3. Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trusted Contacts (${familyMembers.size})",
                        color = NavyText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Family Member Cards
                familyMembers.forEach { member ->
                    FamilyMemberCard(
                        member = member,
                        onLongClick = { selectedMemberForOptions = member }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 5. Add Family Member Button
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "+ Add New Family Member",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0E73F6)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Automatic Scam Safety Alert Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scam Alert Sharing",
                                    color = NavyText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Notify ${primaryMember?.name ?: "caregivers"} if Nudge detects a dangerous SMS scam.",
                                    color = SlateSubtitle,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = autoAlertsEnabled,
                                onCheckedChange = { autoAlertsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0E73F6)
                                )
                            )
                        }

                        if (autoAlertsEnabled && primaryMember != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        val smsUri = Uri.parse("sms:${primaryMember.phoneNumber}?body=${Uri.encode("Hi ${primaryMember.name}, Nudge safety alert: a suspicious message was flagged on my phone. Everything is okay, just keeping you informed!")}")
                                        val intent = Intent(Intent.ACTION_VIEW, smsUri)
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Alert prepared for ${primaryMember.name}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📤 Send Sample Alert SMS to ${primaryMember.name}",
                                    color = Color(0xFF0E73F6),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Member Dialog
    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var relInput by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf("") }
        var isPrimaryInput by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Family Member",
                    color = NavyText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. John Miller") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = relInput,
                        onValueChange = { relInput = it },
                        label = { Text("Relationship") },
                        placeholder = { Text("e.g. Son, Daughter, Neighbor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. 555-019-2831") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPrimaryInput = !isPrimaryInput }
                    ) {
                        Checkbox(
                            checked = isPrimaryInput,
                            onCheckedChange = { isPrimaryInput = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF21A15C))
                        )
                        Text(
                            text = "Set as Primary Caregiver",
                            color = NavyText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            FamilyRepository.addMember(
                                name = nameInput,
                                relationship = if (relInput.isNotBlank()) relInput else "Family",
                                phone = phoneInput,
                                isPrimary = isPrimaryInput
                            )
                            showAddDialog = false
                            Toast.makeText(context, "$nameInput added!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a name and phone number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E73F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = SlateSubtitle)
                }
            }
        )
    }

    // Member Options Dialog (Delete / Make Primary)
    selectedMemberForOptions?.let { member ->
        AlertDialog(
            onDismissRequest = { selectedMemberForOptions = null },
            title = { Text(text = member.name, fontWeight = FontWeight.Bold, color = NavyText) },
            text = { Text("What would you like to do with this contact?") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!member.isPrimary) {
                        Button(
                            onClick = {
                                FamilyRepository.setPrimary(member.id)
                                selectedMemberForOptions = null
                                Toast.makeText(context, "${member.name} is now Primary Caregiver", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21A15C)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Make Primary")
                        }
                    }
                    Button(
                        onClick = {
                            FamilyRepository.removeMember(member.id)
                            selectedMemberForOptions = null
                            Toast.makeText(context, "${member.name} removed", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForOptions = null }) {
                    Text("Close", color = SlateSubtitle)
                }
            }
        )
    }
}

@Composable
private fun FamilyMemberCard(
    member: FamilyContact,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLongClick() }
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_people_green),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            color = NavyText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (member.isPrimary) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F8F0), shape = RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Primary Helper",
                                    color = Color(0xFF21A15C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = member.relationship,
                        color = SlateSubtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row (Equal Width)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phoneNumber}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Calling ${member.name}...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21A15C)),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "📞 Call",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${member.phoneNumber}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Texting ${member.name}...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "💬 Message",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0E73F6)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 840)
@Composable
fun FamilyScreenPreview() {
    NudgeTheme {
        FamilyScreen()
    }
}
