# OMNIA MAX — Security Audit & Google Play Protect Hardening Plan

This plan performs a complete security audit of OMNIA MAX to eliminate potential Google Play Protect high-risk detection flags, enforce zero-trust privacy controls, remove excessive permissions, secure API keys, and harden the application for safe release.

---

> [!IMPORTANT] User Review & Critical Decisions
>
> 1. **Permission Reduction (Principle of Least Privilege)**:
>    - **Root Cause of Play Protect Warning**: Combining `SEND_SMS`, `READ_CONTACTS`, `RECORD_AUDIO`, and `FOREGROUND_SERVICE_MICROPHONE` in an app that is not a default SMS handler triggers Google Play Protect's spyware/data-harvesting heuristic flags.
>    - **Fix**: Remove `android.permission.SEND_SMS` and `android.permission.READ_CONTACTS` from `AndroidManifest.xml`.
>    - **Replacement**: Use zero-permission Android system `Intent.ACTION_SENDTO` (with `smsto:` payload) for SMS dispatch, and `ActivityResultContracts.PickContact()` for recipient selection.
> 2. **Privacy-First Microphone & Wake-Word Consent**:
>    - Require explicit opt-in user consent card before enabling `MaxWakeWordService`.
>    - Display an un-dismissable foreground notification badge with a prominent "Turn Off" button while the microphone listener is active.
>    - Ensure raw audio is processed strictly on-device for wake-word matching and never recorded or uploaded to remote servers.
> 3. **API Key & Network Security**:
>    - Store OpenRouter and provider API keys in Android Keystore-backed `EncryptedSharedPreferences`.
>    - Suppress sensitive key headers from HTTP logging interceptors in release builds.

---

## 1. Security Audit Findings & Vulnerability Analysis

| Audit Area | Findings & Risk Level | Remediation Action |
| :--- | :--- | :--- |
| **Permissions** | **HIGH RISK**: `SEND_SMS` & `READ_CONTACTS` combined with background microphone permission triggers Play Protect malware heuristics. | Remove `SEND_SMS` and `READ_CONTACTS` from Manifest. Replace with system `Intent.ACTION_SENDTO` and contact pickers. |
| **Microphone / Wake-Word** | **MEDIUM RISK**: `MaxWakeWordService` starts without prior consent dialog. | Implement explicit Privacy Consent Modal prior to activating background service. |
| **API Key Credentials** | **MEDIUM RISK**: Direct API key storage in plaintext SharedPreferences risks credential leakage. | Enforce `EncryptedSharedPreferences` backed by Android Keystore. Mask API keys in logs. |
| **Network Requests** | **LOW RISK**: Ensure all API calls use HTTPS with valid TLS certificates. | Validate Retrofit/OkHttp HTTPS endpoints for OpenRouter and Gemini API gateway. |
| **Exported Components** | **LOW RISK**: `MainActivity` is exported for launcher; `MaxWakeWordService` is non-exported (`android:exported="false"`). | Retain non-exported guard on background service. |

---

## 2. User Experience & Privacy Safeguards

- **Permission Rationale Dialogs**:
  - When the user triggers voice assistant features, present clear Compose rationale dialogs explaining why microphone access is required.
  - If permissions are denied, degrade gracefully to text input mode without crashing.
- **Microphone Privacy Indicator**:
  - Display active microphone state on the `AiOrbView` UI and in the persistent system notification bar.
  - Provide a one-tap "Stop Listening" action in both the UI and notification.

---

## 3. Technical Architecture & Security Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        USER INPUT / VOICE COMMAND                      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    ZERO-PERMISSION INTENT DELEGATION                   │
│   - SMS: Intent.ACTION_SENDTO (smsto:uri) -> Direct System Composer      │
│   - Contacts: ActivityResultContracts.PickContact()                    │
│   - Social Apps: Native Share/Message Intents                          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     ANDROID KEYSTORE SECRET STORE                      │
│   - EncryptedSharedPreferences (MasterKey AES-256)                    │
│   - Redacted OkHttp Logging Headers (Authorization: Bearer ***)        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 PRIVACY-FIRST FOREGROUND SERVICE                       │
│   - MaxWakeWordService (Non-Exported)                                  │
│   - User Opt-In Consent Verification                                  │
│   - On-Device Audio Processing                                         │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Implementation Steps

1. **Manifest Hardening (`AndroidManifest.xml`)**:
   - Remove `<uses-permission android:name="android.permission.SEND_SMS" />`
   - Remove `<uses-permission android:name="android.permission.READ_CONTACTS" />`
   - Verify all remaining permissions (`RECORD_AUDIO`, `INTERNET`, `ACCESS_NETWORK_STATE`, `FLASHLIGHT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE`).
2. **Messaging & Contacts Adapter Update**:
   - Refactor `MessagingDispatcher.kt` to send SMS using system Intent delegation (`Intent.ACTION_SENDTO`) without requiring `SEND_SMS`.
   - Refactor `ContactResolver.kt` to remove direct database queries on `ContactsContract` that require `READ_CONTACTS`.
3. **Secure Storage Hardening (`SecurePreferencesManager.kt`)**:
   - Verify AES-256 GCM encryption via Android Keystore `MasterKeys` / `MasterKey.Builder`.
   - Ensure cleartext fallback keys are strictly avoided.
4. **Network & Logging Cleanup**:
   - Mask `Authorization` headers in OkHttp logging interceptors.
5. **Build Verification**:
   - Run `compile_applet` to confirm zero compilation or lint errors.
