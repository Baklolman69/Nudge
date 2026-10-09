package com.tensormind.nudge.diagnostics

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.tensormind.nudge.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ActivityLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val timestamp: String,
    val category: String, // "FIX", "SCAM", "CHECKUP"
    val badgeText: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val iconRes: Int
)

data class FamilyContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val isPrimary: Boolean
)

object NudgeActivityRepository {
    val activities = mutableStateListOf<ActivityLogEntry>()

    init {
        populateDefaults()
    }

    fun populateDefaults() {
        activities.clear()
        activities.addAll(
            listOf(
                ActivityLogEntry(
                    title = "Sound Restored to 85%",
                    description = "Ringer volume was muted and DND policy was active. Both restored automatically with confirmation chime.",
                    timestamp = "10 mins ago",
                    category = "FIX",
                    badgeText = "Sound Fixed",
                    badgeBgColor = Color(0xFFE8F8F0),
                    badgeTextColor = Color(0xFF21A15C),
                    iconRes = R.drawable.ic_volume_red
                ),
                ActivityLogEntry(
                    title = "Phishing SMS Evaluated",
                    description = "Analyzed fake bank card suspension link using Qwen 2.5 on Groq. Flagged as Dangerous.",
                    timestamp = "1 hour ago",
                    category = "SCAM",
                    badgeText = "Scam Blocked",
                    badgeBgColor = Color(0xFFFFEEEE),
                    badgeTextColor = Color(0xFFE53935),
                    iconRes = R.drawable.ic_shield_purple
                ),
                ActivityLogEntry(
                    title = "Full System Diagnostic Checkup",
                    description = "Battery: 84%, Storage: 24.8GB free, Network: Connected to Wi-Fi. System overall score: Excellent.",
                    timestamp = "Today, 11:20 AM",
                    category = "CHECKUP",
                    badgeText = "Health Scan",
                    badgeBgColor = Color(0xFFEBF5FF),
                    badgeTextColor = Color(0xFF0E73F6),
                    iconRes = R.drawable.ic_sparkles
                )
            )
        )
    }

    fun logActivity(
        title: String,
        description: String,
        category: String,
        badgeText: String,
        badgeBg: Color,
        badgeTextCol: Color,
        iconRes: Int
    ) {
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        activities.add(
            0,
            ActivityLogEntry(
                title = title,
                description = description,
                timestamp = "Today, $now",
                category = category,
                badgeText = badgeText,
                badgeBgColor = badgeBg,
                badgeTextColor = badgeTextCol,
                iconRes = iconRes
            )
        )
    }

    fun clearAll() {
        activities.clear()
    }
}

object FamilyRepository {
    val familyMembers = mutableStateListOf<FamilyContact>()

    init {
        populateDefaults()
    }

    fun populateDefaults() {
        familyMembers.clear()
        familyMembers.addAll(
            listOf(
                FamilyContact(
                    name = "Sarah Miller",
                    relationship = "Daughter",
                    phoneNumber = "5550192831",
                    isPrimary = true
                ),
                FamilyContact(
                    name = "Michael Miller",
                    relationship = "Son",
                    phoneNumber = "5550129482",
                    isPrimary = false
                ),
                FamilyContact(
                    name = "Alex Miller",
                    relationship = "Grandson (Tech Assistant)",
                    phoneNumber = "5550183310",
                    isPrimary = false
                )
            )
        )
    }

    fun addMember(name: String, relationship: String, phone: String, isPrimary: Boolean) {
        if (isPrimary) {
            // Unset previous primary
            val updated = familyMembers.map { it.copy(isPrimary = false) }
            familyMembers.clear()
            familyMembers.addAll(updated)
        }
        familyMembers.add(
            FamilyContact(
                name = name.trim(),
                relationship = relationship.trim(),
                phoneNumber = phone.trim(),
                isPrimary = isPrimary
            )
        )
    }

    fun removeMember(id: String) {
        familyMembers.removeAll { it.id == id }
    }

    fun setPrimary(id: String) {
        val updated = familyMembers.map { it.copy(isPrimary = (it.id == id)) }
        familyMembers.clear()
        familyMembers.addAll(updated)
    }

    fun getPrimaryMember(): FamilyContact? {
        return familyMembers.firstOrNull { it.isPrimary } ?: familyMembers.firstOrNull()
    }
}
