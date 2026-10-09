# 🌸 Nudge — Your Friendly AI Phone Guardian
*Devpost Project Story Submission*

---

## Inspiration

Modern smartphones have become unnecessarily intimidating. For our grandparents and older loved ones, a standard Android device contains over 300 nested settings menus spanning Do Not Disturb profiles, volume attenuation channels, notification filters, and Bluetooth routing.

Everyone who has an elderly parent or grandparent knows the familiar, stressful phone call:
*"Can you hear me? My phone stopped ringing. I think I broke it."*

An accidental swipe or button press mutes the ringer, leading to missed calls from doctors or family members and creating real anxiety. At the same time, aggressive SMS phishing scams (*"Your bank account has been suspended! Tap here immediately"*) deliberately prey on digital unfamiliarity and urgency.

When seniors turn to traditional AI chatbots for help, they get bombarded with 10-step technical instructions full of confusing jargon like *SSID*, *A2DP*, or *interruption policies*. We wanted to build something fundamentally different: **an empathetic on-device companion that doesn't just chat, but takes safe action on the phone and speaks back in warm, reassuring plain English.**

---

## What it does

**Nudge** is an elder-centric, voice-first on-device action guardian. Instead of telling users how to navigate settings, Nudge inspects hardware states directly, repairs them with a single tap, and explains what happened with zero technical jargon.

### 🌟 Core Capabilities:
1. **🔊 1-Tap Sound Doctor**: 
   Inspects `AudioManager` across all sound channels and detects Do Not Disturb call blocking. With one tap, it restores ringer and media volumes to a comfortable 85% and plays a cheerful musical chime (`ToneGenerator`) so the user immediately hears that their phone works again.
2. **🛡️ AI Safety Shield (Scam & Phishing Detector)**: 
   Powered by **Groq-accelerated Qwen 2.5**, Nudge evaluates suspicious text messages in under 500ms into clear ratings (🟢 Safe, 🟡 Suspicious, 🔴 Dangerous). It explains the danger in 1–2 plain-English sentences and integrates with the Android system **Share Sheet** (`ACTION_SEND`) so users can verify messages directly from their messaging app with zero copy-pasting. If offline, an internal heuristic pattern matcher keeps them safe.
3. **🧠 Autonomous Multi-Step Skills Engine**: 
   Executes an agentic $\text{Observe} \rightarrow \text{Act} \rightarrow \text{Verify} \rightarrow \text{Report}$ self-healing loop. It diagnoses network reachability with live socket pings, optimizes RAM, clears cached app junk, toggles the flashlight, and monitors battery thermals.
4. **🎙️ Natural Voice & Chat Assistant ("Grandpa Mode")**: 
   Features a native speech-recognition modal with animated acoustic ripples and a conversational chat assistant. Spoken voice (TTS) is calibrated to a slower pace (0.85x) and higher pitch clarity tailored for aging ears.
5. **👨‍👩‍👧 Family & Caregiver Connect**: 
   Provides oversized 1-tap call and SMS buttons for primary family contacts, plus an emergency SOS trigger that formats a status report with battery and device metrics.
6. **👵 Elder-First Tactile UI**: 
   Soft pastel color palette (Warm Rose, Soft Mint, Butter Yellow, Sky Blue), oversized touch cards (22dp+ padding) for shaky hands, and a friendly breathing mascot companion.

---

## How we built it

We built Nudge using the **Devpost Learn Skill Pack** and the plan-first flipped-interaction curriculum:

1. **Plan-First Architecture**:
   - **Scope (`devpost/scope.md`)**: Locked the core thesis—an on-device action agent that repairs hardware states directly vs. advice chatbots that provide frustrating instructions—and established a strict proof-of-concept boundary.
   - **PRD (`devpost/prd.md`)**: Specified the complete senior journey, tactile UI interaction states, and strict zero-jargon copy guidelines.
   - **Spec (`devpost/spec.md`)**: Designed the component architecture, data contracts, and technical stack.
   - **Checklist (`devpost/checklist.md`)**: Implemented and verified the application across 5 ordered slices.

2. **Technology Stack**:
   - **UI & Architecture**: 100% Kotlin 2.0.21 and Jetpack Compose with Material Design 3, structured using MVI / MVVM reactive patterns.
   - **Hardware Diagnostics**: Direct integration with Android system services: `AudioManager`, `NotificationManager`, `ConnectivityManager`, `BatteryManager`, `CameraManager`, and `StatFs`.
   - **High-Speed AI Inference**: [Groq Cloud](https://groq.com) running `Qwen 2.5` with strict JSON mode formatting and low temperature (0.2) for predictable, sub-500ms safety evaluations.
   - **Speech Stack**: Native Android `SpeechRecognizer` paired with senior-tuned `TextToSpeech` and harmonic tone synthesis via `ToneGenerator`.
   - **Multi-Language Engine**: Custom `LanguageManager` supporting 5 languages (English, Spanish, Hindi, French, German) with real-time cached translation.

---

## Challenges we ran into

1. **Android Do Not Disturb Policies & OEM Variations**: 
   Android's notification policy access (`NotificationManager.isNotificationPolicyAccessGranted`) varies across Android versions (API 24 to 35) and manufacturer skins. We had to architect `AudioDiagnosticService` to gracefully adapt—restoring unconstrained volume streams immediately and guiding the user with 1 tap if DND special access was needed.
2. **Sub-500ms Latency for Anxious Users**: 
   Older adults become anxious when a screen shows a spinning wheel for several seconds. We integrated Groq's high-speed LPU inference with `Qwen 2.5` to ensure scam analysis returns structured JSON in under 500ms, paired with dynamic step-by-step progress pills ("Thinking...", "Checking links...", "Verified!").
3. **Designing for Aging Eyes and Hands**: 
   Standard Material components often have small 48dp touch targets and thin contrast. We redesigned every interactive element with 22dp+ padding, high-contrast navy typography, and calming pastels, ensuring users with arthritis or tremors never mis-tap.
4. **Resilient Offline Architecture**: 
   Seniors frequently toggle Airplane mode by mistake. We built a local regex and fraud-keyword engine into `GroqQwenClient` and socket reachability tests in `NetworkDiagnosticService` so Nudge remains useful even with zero network connectivity.

---

## Accomplishments that we're proud of

- **An Action Agent, Not a Text Generator**: 
  Nudge doesn't say *"Go to Settings > Sound"*; it un-mutes the ringer, sets safe volume levels, and immediately rings a confirmation chime. Hearing the chime play brings instant relief.
- **Privacy-Preserving Scam Detection**: 
  Nudge provides scam protection without requiring dangerous `READ_SMS` or `READ_CALL_LOG` permissions. Leveraging the Android system **Share Sheet** (`ACTION_SEND`) gives users complete control over what they share.
- **Strict Plan-First Execution**: 
  We stuck strictly to the Devpost Learn planning curriculum. All planning documents in `devpost/` accurately reflect the finished product without unplanned feature creep.
- **Zero Local Dependencies or Root**: 
  Runs safely within standard Android OS sandboxing on Android 7.0 through Android 15.

---

## What we learned

- **The Power of Flipped Interaction**: 
  Planning before coding transformed the development process. Walking through the scoping interview and PRD forced us to define the exact proof-of-concept boundary before writing a single line of Compose code.
- **Structuring LLM Output for Action Engines**: 
  Prompting fast models like Qwen 2.5 on Groq with explicit JSON schemas allows seamless bridging between cloud reasoning and on-device hardware execution.
- **Accessibility is an Architecture Decision**: 
  Elder accessibility isn't just large fonts—it requires multimodal feedback (visual badges + spoken voice + musical tones) and zero technical terminology.

---

## What's next for Nudge — Your Friendly AI Phone Guardian

- **👨‍👩‍👧 Remote Caregiver Push Sync (Firebase)**: 
  Optional real-time alerts sent to adult children when an issue requires physical help (e.g. a damaged physical SIM card or battery health degradation).
- **🫧 Floating Accessibility Bubble**: 
  A floating mascot widget overlay that can be summoned from any screen or app on the phone with one tap.
- **🗣️ Multilingual Neural Speech**: 
  Expanding the English TTS engine to native neural voice models in Spanish, Hindi, French, and German.
- **📞 Automated Spam Call Screener**: 
  Real-time incoming call screening against known fraud caller registries.
