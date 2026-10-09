package com.tensormind.nudge.diagnostics

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.tensormind.nudge.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ActivityLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val timestamp: String,
    val category: String, // "FIX", "SCAM", "CHECKUP", "BOOST"
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
    private var appContext: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val prefs = appContext?.getSharedPreferences("nudge_activity_prefs", Context.MODE_PRIVATE)
            ?: return
        val jsonStr = prefs.getString("activities_json", null)
        if (jsonStr.isNullOrBlank()) {
            populateDefaults()
            return
        }

        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<ActivityLogEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ActivityLogEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        timestamp = obj.getString("timestamp"),
                        category = obj.getString("category"),
                        badgeText = obj.getString("badgeText"),
                        badgeBgColor = Color(obj.getInt("badgeBgColor")),
                        badgeTextColor = Color(obj.getInt("badgeTextColor")),
                        iconRes = obj.optInt("iconRes", R.drawable.ic_sparkles)
                    )
                )
            }
            mainHandler.post {
                activities.clear()
                activities.addAll(list)
            }
        } catch (_: Exception) {
            populateDefaults()
        }
    }

    private fun saveToPrefs() {
        val ctx = appContext ?: return
        try {
            val arr = JSONArray()
            activities.take(30).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("description", item.description)
                    put("timestamp", item.timestamp)
                    put("category", item.category)
                    put("badgeText", item.badgeText)
                    put("badgeBgColor", item.badgeBgColor.toArgb())
                    put("badgeTextColor", item.badgeTextColor.toArgb())
                    put("iconRes", item.iconRes)
                }
                arr.put(obj)
            }
            ctx.getSharedPreferences("nudge_activity_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("activities_json", arr.toString())
                .apply()
        } catch (_: Exception) {}
    }

    init {
        populateDefaults()
    }

    fun populateDefaults() {
        mainHandler.post {
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
        val entry = ActivityLogEntry(
            title = title,
            description = description,
            timestamp = "Today, $now",
            category = category,
            badgeText = badgeText,
            badgeBgColor = badgeBg,
            badgeTextColor = badgeTextCol,
            iconRes = iconRes
        )
        mainHandler.post {
            activities.add(0, entry)
            saveToPrefs()
        }
    }

    fun clearAll() {
        mainHandler.post {
            activities.clear()
            saveToPrefs()
        }
    }
}

object FamilyRepository {
    val familyMembers = mutableStateListOf<FamilyContact>()
    private var appContext: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val prefs = appContext?.getSharedPreferences("nudge_family_prefs", Context.MODE_PRIVATE)
            ?: return
        val jsonStr = prefs.getString("family_json", null)
        if (jsonStr.isNullOrBlank()) {
            populateDefaults()
            return
        }

        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<FamilyContact>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    FamilyContact(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.getString("name"),
                        relationship = obj.getString("relationship"),
                        phoneNumber = obj.getString("phoneNumber"),
                        isPrimary = obj.optBoolean("isPrimary", false)
                    )
                )
            }
            mainHandler.post {
                familyMembers.clear()
                familyMembers.addAll(list)
            }
        } catch (_: Exception) {
            populateDefaults()
        }
    }

    private fun saveToPrefs() {
        val ctx = appContext ?: return
        try {
            val arr = JSONArray()
            familyMembers.forEach { member ->
                val obj = JSONObject().apply {
                    put("id", member.id)
                    put("name", member.name)
                    put("relationship", member.relationship)
                    put("phoneNumber", member.phoneNumber)
                    put("isPrimary", member.isPrimary)
                }
                arr.put(obj)
            }
            ctx.getSharedPreferences("nudge_family_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("family_json", arr.toString())
                .apply()
        } catch (_: Exception) {}
    }

    init {
        populateDefaults()
    }

    fun populateDefaults() {
        mainHandler.post {
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
    }

    fun addMember(name: String, relationship: String, phone: String, isPrimary: Boolean) {
        mainHandler.post {
            if (isPrimary) {
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
            saveToPrefs()
        }
    }

    fun removeMember(id: String) {
        mainHandler.post {
            familyMembers.removeAll { it.id == id }
            saveToPrefs()
        }
    }

    fun setPrimary(id: String) {
        mainHandler.post {
            val updated = familyMembers.map { it.copy(isPrimary = (it.id == id)) }
            familyMembers.clear()
            familyMembers.addAll(updated)
            saveToPrefs()
        }
    }

    fun getPrimaryMember(): FamilyContact? {
        return familyMembers.firstOrNull { it.isPrimary } ?: familyMembers.firstOrNull()
    }
}
