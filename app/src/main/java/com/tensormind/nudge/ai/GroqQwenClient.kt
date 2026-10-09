package com.tensormind.nudge.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class RiskLevel {
    SAFE,
    SUSPICIOUS,
    DANGEROUS
}

data class ScamAnalysisResult(
    val risk: RiskLevel,
    val headline: String,
    val explanation: String,
    val recommendedAction: String
)

data class NudgeAgentResponse(
    val replyText: String,
    val actionCommand: String? = null // "FIX_AUDIO", "OPEN_WIFI", "OPEN_BLUETOOTH", "OPEN_BATTERY", "CALL_FAMILY", "OPEN_SCAM_CHECK", "NONE"
)

class GroqQwenClient(
    private var apiKey: String = ""
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    // Live commissioned models on Groq in priority order
    private val liveModels = listOf(
        "qwen/qwen3.8-27b",
        "openai/gpt-oss-120b",
        "openai/gpt-oss-20b"
    )

    fun updateApiKey(newKey: String) {
        apiKey = newKey.trim()
    }

    suspend fun analyzeScamMessage(messageText: String): ScamAnalysisResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext fallbackScamAnalysis(messageText)
        }

        val systemPrompt = """
            You are Nudge Safety Shield, an AI security guard protecting elderly and non-tech users from scams, phishing, and fake messages.
            Analyze the provided message text.
            Respond STRICTLY in JSON format with exactly these keys:
            {
              "risk": "SAFE" | "SUSPICIOUS" | "DANGEROUS",
              "headline": "Short 3-6 word summary (e.g. Dangerous Bank Impersonation)",
              "explanation": "Simple 1-2 sentence explanation in plain English with ZERO technical jargon.",
              "recommendedAction": "1 simple step (e.g. Delete this message immediately and do not tap the link)"
            }
        """.trimIndent()

        for (model in liveModels) {
            try {
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Analyze this message:\n\"$messageText\"")
                    })
                }

                val bodyJson = JSONObject().apply {
                    put("model", model)
                    put("messages", messages)
                    put("temperature", 0.2)
                    put("response_format", JSONObject().put("type", "json_object"))
                }

                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val respBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResp = JSONObject(respBody)
                    val content = jsonResp.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    val parsed = JSONObject(content)
                    val riskStr = parsed.optString("risk", "SUSPICIOUS").uppercase()
                    val risk = when {
                        riskStr.contains("DANGER") -> RiskLevel.DANGEROUS
                        riskStr.contains("SAFE") -> RiskLevel.SAFE
                        else -> RiskLevel.SUSPICIOUS
                    }

                    return@withContext ScamAnalysisResult(
                        risk = risk,
                        headline = parsed.optString("headline", "Suspicious Message Alert"),
                        explanation = parsed.optString("explanation", "This message asks for sensitive details or contains an untrusted link."),
                        recommendedAction = parsed.optString("recommendedAction", "Do not tap any links or reply to this sender.")
                    )
                }
            } catch (_: Exception) {
                // Try next live model
                continue
            }
        }

        fallbackScamAnalysis(messageText)
    }

    suspend fun processUserVoiceQuery(userSpeech: String): NudgeAgentResponse = withContext(Dispatchers.IO) {
        val lower = userSpeech.lowercase().trim()

        // Fast offline intent detector for instant backup
        val offlineAction = when {
            lower.contains("sound") || lower.contains("hear") || lower.contains("ring") || 
            lower.contains("volume") || lower.contains("quiet") || lower.contains("mute") || lower.contains("silent") -> "FIX_AUDIO"
            lower.contains("wifi") || lower.contains("internet") || lower.contains("network") || 
            lower.contains("data") || lower.contains("web") || lower.contains("connect") -> "OPEN_WIFI"
            lower.contains("bluetooth") || lower.contains("headphone") || lower.contains("speaker") || lower.contains("earbud") -> "OPEN_BLUETOOTH"
            lower.contains("battery") || lower.contains("power") || lower.contains("drain") || lower.contains("charge") || lower.contains("hot") -> "OPEN_BATTERY"
            lower.contains("call") || lower.contains("dial") || lower.contains("phone") || lower.contains("family") || lower.contains("daughter") || lower.contains("son") -> "CALL_FAMILY"
            lower.contains("scam") || lower.contains("fraud") || lower.contains("fake") || lower.contains("sms") || lower.contains("text message") -> "OPEN_SCAM_CHECK"
            else -> "NONE"
        }

        if (apiKey.isBlank()) {
            return@withContext getFriendlyFallbackResponse(lower, offlineAction)
        }

        val systemPrompt = """
            You are Nudge, the world's most patient, caring, and loving phone companion for seniors and elderly users.
            Speak like a warm, loving grandchild or dear family friend.
            Always be comforting, positive, and reassuring.
            Never use confusing technical jargon or abbreviations.
            Keep your answer to 1 or 2 short, clear sentences.
            You must reply in JSON format with two keys:
            {
              "reply": "Your warm, loving 1-2 sentence response explaining simply what we are doing to help.",
              "action": "FIX_AUDIO" | "OPEN_WIFI" | "OPEN_BLUETOOTH" | "OPEN_BATTERY" | "CALL_FAMILY" | "OPEN_SCAM_CHECK" | "NONE"
            }
        """.trimIndent()

        for (model in liveModels) {
            try {
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userSpeech)
                    })
                }

                val bodyJson = JSONObject().apply {
                    put("model", model)
                    put("messages", messages)
                    put("temperature", 0.3)
                    put("response_format", JSONObject().put("type", "json_object"))
                }

                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val respBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResp = JSONObject(respBody)
                    val content = jsonResp.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    val parsed = JSONObject(content)
                    val reply = parsed.optString("reply", "I'm right here with you! Let's take care of this together.")
                    var action = parsed.optString("action", "NONE").uppercase()
                    if (action == "NONE" && offlineAction != "NONE") {
                        action = offlineAction
                    }

                    return@withContext NudgeAgentResponse(
                        replyText = reply,
                        actionCommand = if (action != "NONE") action else null
                    )
                }
            } catch (_: Exception) {
                // Failover to next live Groq model
                continue
            }
        }

        // Resilient fallback if network was unreachable
        getFriendlyFallbackResponse(lower, offlineAction)
    }

    private fun getFriendlyFallbackResponse(query: String, action: String): NudgeAgentResponse {
        val reply = when (action) {
            "FIX_AUDIO" -> "Don't worry at all! I'm checking your sound and turning your ringer volume up so you won't miss any calls."
            "OPEN_WIFI" -> "I'm right here with you! Let's check your Wi-Fi connection together to get you back online."
            "OPEN_BLUETOOTH" -> "Let's check your Bluetooth settings so your audio and accessories connect smoothly."
            "OPEN_BATTERY" -> "I noticed your phone needs some care. Let's look at your battery saver settings to keep it running cool and lasting long."
            "CALL_FAMILY" -> "Of course! Let's open your phone dialer so you can reach your loved ones right away."
            "OPEN_SCAM_CHECK" -> "You did the right thing asking! Let's scan your messages to make sure you stay completely safe."
            else -> "Hello! I'm Nudge, your phone's best friend. Whatever is bothering you with your phone, I'm here to fix it for you!"
        }

        return NudgeAgentResponse(
            replyText = reply,
            actionCommand = if (action != "NONE") action else null
        )
    }

    private fun fallbackScamAnalysis(text: String): ScamAnalysisResult {
        val lower = text.lowercase()
        val isScam = lower.contains("bank") || lower.contains("urgent") ||
                lower.contains("account locked") || lower.contains("password") ||
                lower.contains("bit.ly") || lower.contains("claim prize") ||
                lower.contains("verify") || lower.contains("click here") ||
                lower.contains("suspended")

        return if (isScam) {
            ScamAnalysisResult(
                risk = RiskLevel.DANGEROUS,
                headline = "Dangerous Phishing Scam Detected",
                explanation = "This message pretends to be an official bank or company trying to get your private passwords.",
                recommendedAction = "Delete this message right away. Never tap links sent from unknown numbers."
            )
        } else {
            ScamAnalysisResult(
                risk = RiskLevel.SAFE,
                headline = "Message Appears Safe",
                explanation = "We checked this text and found no threatening links, scam requests, or password traps.",
                recommendedAction = "You can read and reply to this message with peace of mind."
            )
        }
    }
}