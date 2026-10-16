# MAX Personal Assistant — Google Assistant-Inspired Voice & Device Assistant

Transform OMNIA MAX into a full-fledged, voice-first Google Assistant-inspired personal assistant named **MAX**. MAX combines real-time speech recognition, continuous/push-to-talk wake-word listening, Android device controls, messaging preparation and sending across WhatsApp, SMS, Instagram, and Messenger, and OpenRouter AI multi-model intelligence for general reasoning.

---

> [!IMPORTANT] User Review & Critical Decisions
>
> 1. **Voice Interaction Strategy**: Push-To-Talk standard with a configurable continuous wake-word listener ("Hey Max") running in an active Foreground Service when enabled.
> 2. **Device Command Confirmations**: Read-only actions (battery, clock, volume check) execute deterministically with spoken feedback. Actions with external effects (sending messages, app launches, settings toggles) present a visual voice overlay confirmation dialog with read-back audio.
> 3. **Messaging Fallback Logic**: If a messaging platform SDK/Intent direct send is restricted by third-party Android sandboxing, MAX automatically pre-populates the recipient and exact message text in the target app's native sharing or message intent, guiding the user to tap send.

---

## 1. Overview & Core Concept

- **What It Does**: MAX acts as an intelligent Android Personal Assistant. It listens to spoken or typed voice commands, resolves recipient contacts, executes device commands (flashlight, volume, alarms, timers, app opening, settings, battery), formats and sends personal messages, and falls back to OpenRouter multi-model AI for complex reasoning.
- **Target Audience**: Android users who want a unified voice assistant capable of hands-free interaction, system utility control, multi-platform messaging assistance, and deep AI reasoning.
- **Key Value**: Operates on real public Android APIs, respects device safety and privacy, eliminates guesswork through explicit message read-backs, and preserves existing OMNIA MAX multi-model chat history and settings.

---

## 2. User Experience & Visual Design

- **Voice Assistant Overlay & HUD**:
  - A bottom sheet / floating overlay powered by the multi-state animated `AiOrbView` (idle, listening, processing, speaking, success, error).
  - Dynamic audio waveform animation and live speech-to-text transcript feedback during voice capture.
  - Spoken Text-To-Speech (TTS) responses using `android.speech.tts.TextToSpeech`.
- **Messaging Confirmation Flow**:
  - When user says "Send a WhatsApp message to Rahul saying I'll be late", MAX resolves contact candidates.
  - If multiple matches exist, displays a contact selector card.
  - Reads back the final recipient and exact message string aloud ("Ready to send to Rahul on WhatsApp: 'I'll be late'. Say Send or Cancel.").
  - Interactive "Send Now", "Edit", and "Cancel" buttons with voice recognition listener active for "Send" / "Cancel".
- **Assistant Settings & Privacy**:
  - Dedicated Assistant Settings panel to toggle "Hey Max" Wake Word, TTS Speech Speed & Voice Pitch, Lock Screen Voice Responses, and Permission Status indicators.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Real Speech Recognition vs. Web Audio / External API**
  - *Chosen Approach*: Use Android's native `SpeechRecognizer` API for zero-latency, offline/on-device spoken command recognition.
  - *Why*: Native `SpeechRecognizer` integrates seamlessly with Android audio focus, supports continuous phrase detection, and works standard without requiring paid external speech APIs.
- **Decision 2: Deterministic Command Engine + OpenRouter AI Fallback**
  - *Chosen Approach*: First run spoken commands through a regex/rule-based natural language parser (`AssistantCommandParser`). If a device or messaging intent matches, execute locally with 100% deterministic reliability. If no local command matches, send the context to OpenRouter AI for conversational completion.
  - *Why*: Prevents AI hallucinations when setting timers or checking battery, while allowing full AI intelligence for general queries.
- **Decision 3: Android Permission Safety & Platform Intents**
  - *Chosen Approach*: Request necessary runtime permissions (`RECORD_AUDIO`, `READ_CONTACTS`, `POST_NOTIFICATIONS`, `SEND_SMS`) dynamically in Compose. For restricted third-party messaging apps (WhatsApp, Instagram, Messenger), open native intents with pre-filled payload and target recipient.
  - *Why*: Strict adherence to Google Play policies—no private accessibility scraping or root exploits.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        USER VOICE / TEXT INPUT                         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        MAX VOICE ASSISTANT ENGINE                      │
│   - SpeechRecognizer (On-Device Voice Input)                           │
│   - TextToSpeech (Audio Response Manager)                              │
│   - MaxWakeWordService (Foreground Listener for "Hey Max")             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        ASSISTANT COMMAND PARSER                        │
│   Parses intents: Device Controls, Messaging, Apps, Settings, AI      │
└───────────┬──────────────────────────────────────────┬─────────────────┘
            │ Local Intent Match                       │ General Query
            ▼                                          ▼
┌─────────────────────────┐               ┌──────────────────────────────┐
│  DEVICE CONTROL ENGINE  │               │   OPENROUTER AI ENGINE       │
│  - Flashlight / Volume  │               │   - Multi-Model LLM Gateway  │
│  - Alarm / Timer        │               │   - System Prompt Context    │
│  - App Launcher         │               │   - Streamed Response        │
│  - Settings Intents     │               └──────────────┬───────────────┘
│  - Battery / Clock      │                              │
└───────────┬─────────────┘                              │
            │                                            │
            ▼                                            │
┌─────────────────────────┐                              │
│ MESSAGING DISPATCHER    │                              │
│ - Contact Resolver      │                              │
│ - Read-Back & Confirm   │                              │
│ - WhatsApp / SMS / IG   │                              │
└───────────┬─────────────┘                              │
            │                                            │
            └──────────────────────┬─────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       JETPACK COMPOSE ASSISTANT HUD                    │
│   - Animated AiOrbView (Listening, Processing, Speaking)              │
│   - Live Waveforms & Read-Back Confirmation Cards                      │
│   - Assistant Preferences & Voice Controls                             │
└────────────────────────────────────────────────────────────────────────┘
```

### Planned Components
1. `com.example.assistant.engine.VoiceAssistantManager`: Manages `SpeechRecognizer`, `TextToSpeech`, and listening states.
2. `com.example.assistant.engine.AssistantCommandParser`: Evaluates text strings and classifies into `AssistantCommand` types (`DeviceControl`, `SendMessage`, `OpenApp`, `GeneralAI`).
3. `com.example.assistant.engine.DeviceControlExecutor`: Handles Android system API calls (`CameraManager` for flashlight, `AudioManager` for volume, `AlarmClock` intents, `BatteryManager`, `Settings` intents).
4. `com.example.assistant.messaging.MessagingDispatcher`: Resolves contacts using `ContactsContract`, validates recipient, prepares SMS/WhatsApp/Instagram/Messenger intents, and manages confirmation UI state.
5. `com.example.assistant.service.MaxWakeWordService`: Android Foreground Service for continuous wake-word monitoring when enabled.
6. `com.example.assistant.ui.AssistantVoiceOverlay`: Compose bottom sheet overlay with visual AI Orb, live transcript, and command execution status cards.
