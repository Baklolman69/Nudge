package com.tensormind.nudge.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class LanguageOption(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val hasTts: Boolean
)

object LanguageManager {
    val supportedLanguages = listOf(
        LanguageOption("en", "English", "🇺🇸", hasTts = true),
        LanguageOption("es", "Español", "🇪🇸", hasTts = false),
        LanguageOption("hi", "हिन्दी", "🇮🇳", hasTts = false),
        LanguageOption("fr", "Français", "🇫🇷", hasTts = false),
        LanguageOption("de", "Deutsch", "🇩🇪", hasTts = false)
    )

    var currentLanguageCode by mutableStateOf("en")
        private set

    fun setLanguage(code: String) {
        currentLanguageCode = code
    }

    fun isTtsSupported(): Boolean {
        return currentLanguageCode == "en"
    }

    fun getSelectedLanguage(): LanguageOption {
        return supportedLanguages.firstOrNull { it.code == currentLanguageCode } ?: supportedLanguages[0]
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val translationCache = java.util.concurrent.ConcurrentHashMap<Pair<String, String>, String>()

    // Free Google Translate API endpoint (client=gtx)
    suspend fun translate(text: String, targetLang: String = currentLanguageCode): String = withContext(Dispatchers.IO) {
        if (targetLang == "en" || text.isBlank()) return@withContext text
        val cacheKey = Pair(text, targetLang)
        translationCache[cacheKey]?.let { return@withContext it }

        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encoded"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val body = httpClient.newCall(request).execute().use { response ->
                response.body?.string()
            } ?: return@withContext text

            val jsonArray = JSONArray(body)
            val segments = jsonArray.getJSONArray(0)
            val result = StringBuilder()
            for (i in 0 until segments.length()) {
                result.append(segments.getJSONArray(i).getString(0))
            }
            val translated = result.toString().trim()
            if (translated.isNotBlank()) {
                translationCache[cacheKey] = translated
                translated
            } else {
                text
            }
        } catch (_: Exception) {
            text // Resilient fallback to original text if offline
        }
    }
}
