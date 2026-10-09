---
doc: scope
status: approved
---

# Nudge

A patient, voice-activated AI phone guardian for older adults and non-tech users that diagnoses system settings, restores audio, checks internet connectivity, and protects against phishing scams.

## The Unique Kernel
Instead of generic advice ("Go to Settings > Sound"), Nudge acts as an on-device agent: it directly inspects device hardware states (volume channels, DND policy, active networks), fixes them with one tap, and speaks back in warm, reassuring plain English.

## Who It's For
Older adults (65+) and non-tech individuals who get overwhelmed or anxious by complex nested smartphone settings, fear they "broke their phone", or get targeted by SMS phishing scams.

## The Core Loop
1. User notices something wrong (e.g. phone won't ring) and taps the big microphone or a trouble card.
2. Nudge inspects the phone's system status in milliseconds.
3. Nudge presents a 1-tap fix and speaks what it did in simple English ("I turned your ringer back up!").
4. The user feels relief and gets back to their day.

## Inspiration & Identity
- **Tone**: Warm, patient, reassuring, empathetic.
- **Visuals**: Soft pastels (soft rose, sky blue, butter yellow, mint green), rounded corners (22dp), friendly illustrated mascot and grandpa character.
- **Copy**: Zero technical jargon. Never say "A2DP profile", "latency", or "IP configuration".

## Why This Matters to the Learner
Everyone has spent hours troubleshooting phones for elderly parents or grandparents. Nudge solves a universal pain point and gives seniors independent confidence with technology.

## What "Working" Looks Like
A working Android app where:
1. Tapping "My phone is not making sound" reads live volume, identifies muted state, and restores sound to 85% with an audible chime and voice feedback.
2. Tapping "Is this message safe?" lets the user evaluate suspicious text against Qwen 2.5 on Groq AI, showing a clear risk rating.
3. Tapping the microphone listens to voice input and gives a warm spoken response.

## The POC Boundary
- **In**: Audio diagnostics & 1-tap restore, network status inspector, scam & phishing risk analyzer (Qwen 2.5 on Groq), voice input (STT) and output (TTS), full phone health checkup.
- **Out for POC**: Wireless ADB / Shizuku system debloating, background daemon service, remote caregiver cloud syncing.

## Later
- Remote caregiver sync where family members receive notifications if an issue requires physical help (e.g. damaged SIM card).
- Floating accessibility bubble across other apps.

## Explicitly Cut
- Root-level shell modifications (Android OS security sandbox forbids this for regular apps without complex setup).
