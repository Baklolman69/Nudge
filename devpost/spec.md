---
doc: spec
status: approved
---

# Nudge — Technical Spec

## How This Works, In Plain Language
Nudge is built like a friendly doctor for an Android phone. It has three main layers:
1. **The Ears and Voice**: Uses Android's built-in speech recognition to hear what the user says, and text-to-speech to talk back slowly and clearly.
2. **The Stethoscope (Diagnostics)**: Directly checks the phone's hardware settings — volume bars, silent switches, Wi-Fi connections, and battery levels — using standard Android system services.
3. **The Brain (AI Agent)**: Sends complex questions and suspicious messages to the ultra-fast Qwen 2.5 model running on Groq AI, which analyzes problems in milliseconds and translates technical answers into 2 warm, simple sentences.

## The Core Journey Through the System
1. User taps "My phone is not making sound" →
2. `HomeScreen` opens `SoundTroubleshooterSheet` →
3. `AudioDiagnosticService` queries `AudioManager` for `STREAM_RING` and `RINGER_MODE` →
4. User taps "Fix All Sound Issues Now" →
5. `AudioDiagnosticService.fixSoundIssues()` updates volume, triggers `RingtoneManager` chime, and triggers `NudgeVoiceManager.speak()` →
6. User hears the chime and spoken voice confirming the phone is fixed.

## Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Android Jetpack Compose (Material3, Compose Compiler 2.0.21)
- **Min / Target SDK**: Android SDK 24 / SDK 35
- **Networking**: OkHttp 4.12.0
- **AI Backend**: Groq AI Cloud (`https://api.groq.com/openai/v1/chat/completions`) using model `qwen-2.5-32b`
- **Voice Stack**: Android `SpeechRecognizer` (STT) + `android.speech.tts.TextToSpeech` (TTS)
- **Architecture**: MVI / State-driven UI with domain diagnostic services

## File Structure
```
app/src/main/
├── AndroidManifest.xml             # Permissions (Audio, Network, Mic, Internet)
└── java/com/tensormind/nudge/
    ├── MainActivity.kt             # Edge-to-edge root activity
    ├── ai/
    │   └── GroqQwenClient.kt       # Qwen 2.5 on Groq client & scam analyzer
    ├── diagnostics/
    │   ├── AudioDiagnosticService.kt   # Volume & DND inspector and fixer
    │   ├── NetworkDiagnosticService.kt # ConnectivityManager inspector
    │   └── DeviceHealthService.kt      # Battery & storage health checkup
    ├── voice/
    │   └── NudgeVoiceManager.kt    # SpeechRecognizer + TextToSpeech wrapper
    └── ui/
        ├── components/
        │   ├── FeatureCard.kt
        │   ├── FullCheckupCard.kt
        │   ├── QuickActionCard.kt
        │   ├── SoundTroubleshooterSheet.kt
        │   ├── NetworkTroubleshooterSheet.kt
        │   ├── ScamCheckerSheet.kt
        │   ├── FullCheckupSheet.kt
        │   └── VoiceAssistantDialog.kt
        └── screens/
            ├── HomeScreen.kt       # Main dashboard wiring all sheets & actions
            ├── OnboardingContainer.kt
            └── SplashScreen1-3.kt
```

## External Services and Dependencies
- **Groq API**:
  - Endpoint: `https://api.groq.com/openai/v1/chat/completions`
  - Model: `qwen-2.5-32b`
  - Auth: Bearer token (Groq API Key)
  - Latency: ~300-500 tokens/sec
  - Fallback: Deterministic offline regex pattern heuristics when key is not provided or device is offline.

## Important Failure Modes
1. **No Internet Connection** → Groq calls fall back gracefully to on-device heuristic scam rules and local speech intent matching.
2. **Microphone Permission Denied** → UI prompts user gently or falls back to tapping cards.
3. **Do Not Disturb Permission Required** → Nudge explains in plain words that Android needs permission to adjust silent mode and offers a 1-tap shortcut.
