---
doc: checklist
status: approved
---

# Build Checklist

Build mode: fast

## Slices

- [x] **1. Project Bootstrap & System Dependencies**
  Becomes usable: Project compiles with Gradle, OkHttp, Kotlin Serialization, and required Android permissions.
  Why now: Ensures build stability and permission grants before wiring hardware services.
  PRD ref: `prd.md > Screens and Layout`
  Spec ref: `spec.md > Stack`
  Build: Update `libs.versions.toml`, `build.gradle.kts`, and `AndroidManifest.xml`.
  Verify: `.\gradlew compileDebugKotlin` runs clean.
  Commit: `Add network, permissions, and serialization dependencies`

- [x] **2. Real Android Diagnostic Engines**
  Becomes usable: Code can inspect audio volume streams, ringer mode, network capabilities, battery, and storage space.
  Why now: The core value proposition is actual on-device diagnosis rather than generic chatbot text.
  PRD ref: `prd.md > Features and Behavior` (Sound Troubleshooter)
  Spec ref: `spec.md > Components` (`AudioDiagnosticService`, `NetworkDiagnosticService`, `DeviceHealthService`)
  Build: Implement diagnostic services in `com.tensormind.nudge.diagnostics`.
  Verify: Services read real system parameters and execute audio restoration with chime.
  Commit: `Implement Android diagnostic services for audio, network, and health`

- [x] **3. Groq Qwen 2.5 AI & Scam Risk Analyzer**
  Becomes usable: High-speed LLM client capable of evaluating messages for scams and answering elder voice queries.
  Why now: Connects the intelligent agent layer to the app.
  PRD ref: `prd.md > Features and Behavior` (Scam & Phishing Checker)
  Spec ref: `spec.md > External Services` (`GroqQwenClient`)
  Build: Implement `GroqQwenClient` with JSON mode, prompt engineering, and offline heuristic fallback.
  Verify: Returns structured `ScamAnalysisResult` with risk levels and plain-English recommendations.
  Commit: `Add Groq Qwen 2.5 client with scam detection and offline fallback`

- [x] **4. Open-Source Voice Stack (STT + TTS)**
  Becomes usable: App can listen to spoken English and respond out loud at accessible pitch and speed.
  Why now: Powers the hands-free "Grandpa Mode" experience.
  PRD ref: `prd.md > Features and Behavior` (Voice Assistant)
  Spec ref: `spec.md > Components` (`NudgeVoiceManager`)
  Build: Wrap Android `SpeechRecognizer` and `TextToSpeech` in `NudgeVoiceManager`.
  Verify: Initializes TTS and triggers spoken feedback.
  Commit: `Integrate SpeechRecognizer and TextToSpeech engine`

- [x] **5. Interactive UI Modals & Home Dashboard Integration**
  Becomes usable: All cards and buttons on `HomeScreen` launch functional bottom sheets and dialogs.
  Why now: Completes the full end-to-end user experience ready for demo recording.
  PRD ref: `prd.md > Screens and Layout`
  Spec ref: `spec.md > Components` (UI Sheets)
  Build: Create `SoundTroubleshooterSheet`, `NetworkTroubleshooterSheet`, `ScamCheckerSheet`, `FullCheckupSheet`, and `VoiceAssistantDialog`; wire to `HomeScreen.kt`.
  Verify: Tapping cards opens real interactive sheets with live data and working fix buttons.
  Commit: `Wire interactive sheets and voice assistant into HomeScreen`

## Hands-on Checkpoints
- [x] Early usable behavior explored — Audio troubleshooter verifies hardware volume and chime.
- [x] Final kick-the-tires exploration and feedback completed — All quick actions and voice assistant verified.

## Final Review
- [x] Final review complete — feedback resolved and learner confirms ready to ship.
