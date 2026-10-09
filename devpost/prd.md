---
doc: prd
status: approved
---

# Nudge — Product Requirements

Nudge is an Android companion app that makes troubleshooting everyday phone problems intuitive and stress-free for seniors and non-tech users.

## The Core Journey
1. **Launch**: User opens Nudge, sees friendly mascot greeting and quick-action cards.
2. **Select Issue or Speak**: User taps a quick problem (e.g. "My phone is not making sound") or taps the large microphone.
3. **Automated Inspection**: Nudge reads Android system state in real time (e.g. Ringtone volume = 0%, Silent Mode = Active).
4. **Resolution**: User taps "Fix All Sound Issues Now" — Nudge adjusts volume to 85%, switches ringer mode to Normal, plays a pleasant confirmation chime, and speaks the confirmation out loud.
5. **Safety Check**: User receives a strange text message, taps "Is this message safe?", pastes the text, and receives a plain-English safety verdict (🟢 Safe or 🔴 Dangerous Scam).

## Screens and Layout
- **Onboarding Carousel**: 3 screens introducing Nudge's mission, "Grandpa Mode", and the gentle voice-first philosophy.
- **Home Dashboard**:
  - Top header with mascot logo and settings shortcut.
  - Mascot speech bubble ("What do you need help with today?") + Microphone action button.
  - 2x2 Quick Action grid:
    1. "My phone is not making sound" (Sound troubleshooter).
    2. "My internet isn't working" (Network troubleshooter).
    3. "Is this message safe?" (Scam & phishing checker).
    4. "Call my family" (Quick caregiver reach).
  - "Need a full checkup?" banner card.
  - Reassurance footer card ("You're not alone. We're here to keep your phone simple.").
- **Interactive Modals (Bottom Sheets)**:
  - Sound Troubleshooter Sheet (live volume meters + 1-tap fix).
  - Network Troubleshooter Sheet (Wi-Fi status + settings shortcut).
  - Scam Checker Sheet (text input + Qwen AI risk assessment).
  - Voice Assistant Dialog (speech recognition + Qwen 2.5 response + TTS).
  - Full Checkup Sheet (Battery, Storage, Sound, Network health score).

## Look and Feel
- **Color Palette**: Calm gradients (`#F6F9FE` to `#EDF4FD`), soft accent containers (`#FFF0F2`, `#EBF5FF`, `#FFFBEA`, `#E8F8F0`), strong Navy text (`#0F1D38`).
- **Typography**: Large, bold headings (18sp - 34sp) with comfortable line height for low-vision accessibility.
- **Tone**: Empathetic, supportive, free of tech jargon.

## Features and Behavior

### 1. Sound Troubleshooter
- Real-time detection of `STREAM_RING`, `STREAM_MUSIC`, and `RINGER_MODE`.
- 1-tap restore action resetting audio to 85% and mode to `NORMAL`.
- Audio chime playback to verify speaker output.

### 2. Scam & Phishing Checker
- Powered by Qwen 2.5 on Groq AI (with offline heuristic fallback).
- Evaluates text for urgency tactics, fake bank alerts, credential theft, and suspicious URLs.
- Returns risk level (SAFE, SUSPICIOUS, DANGEROUS), a plain-English headline, and single-step actionable advice.

### 3. Voice Assistant
- Listens to user queries via Android `SpeechRecognizer`.
- Routes queries to Qwen 2.5 with elder-focused system prompt.
- Speaks back using Android `TextToSpeech` at 0.92x speech rate for clarity.

### 4. Full Checkup
- Evaluates battery %, available storage, sound status, and connectivity into an overall score ("Excellent", "Good", "Needs Attention").

## What We're Building (POC)
- End-to-end working Jetpack Compose application.
- Real Android diagnostics engine for audio, network, and battery/storage.
- Working Groq Qwen 2.5 client with fallback.
- Native speech recognition and text-to-speech integration.

## Deferred From the POC
- MediaProjection floating bubble overlay over third-party apps.
- Remote family SMS/cloud sync.

## Non-Goals
- Replacing the Android operating system or carrier settings.
- Requiring root or developer/ADB permissions.
