# OMNIA MAX — Bug Fix & Permanent Settings Storage Plan

This plan details the root causes and architectural fixes for settings persistence, OpenRouter model fetching, manual model ID entry, and app lifecycle state restoration in OMNIA MAX.

---

## User Review & Confirmed Choices

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1 clarification:
> - **API Credential Security**: Android Keystore / `EncryptedSharedPreferences` for secure credential storage.
> - **App Settings Engine**: Jetpack DataStore (`PreferencesDataStore`) for active theme, selected model ID, free models toggle, and system prompts with reactive `Flow` updates.

---

## 1. Root Cause Analysis of Reported Bugs

1. **Settings Resetting Across Lifecycle**:
   - *Cause*: Settings were stored in transient ViewModel state (`MutableStateFlow`) and in memory defaults during `init { ... }` without a persistent DataStore layer. When navigating between screens or restarting the app, ViewModels re-initialized to hardcoded default values (`gemini-3.6-flash`, darkTheme = true).
2. **OpenRouter Model Fetching & ID Visibility**:
   - *Cause*: `ModelFetcher.kt` caught network errors and silently replaced empty lists with hardcoded fallback models instead of preserving actual API responses. Also, exact model IDs (e.g. `google/gemini-2.0-flash-lite:free`) were not displayed separately from human-readable model names.
3. **Missing Manual Model ID Input**:
   - *Cause*: `ProvidersScreen` lacked an explicit manual Model ID text field with a "Test & Save" trigger for cases where automatic fetching is blocked or when using custom endpoint IDs.
4. **Navigation & Process Recreation State Loss**:
   - *Cause*: `MainActivity` used `remember { ChatViewModel(...) }` directly instead of properly scoped ViewModels or DataStore-backed singletons, causing state resets on back navigation.

---

## 2. Proposed Architectural Fixes

### A. Permanent Settings & Encrypted Credential Engine
- **`SecurePreferencesManager`**: Uses Android Keystore to encrypt API keys (OpenAI, OpenRouter, Custom) so private credentials are never stored in plain text.
- **`AppPreferencesRepository`**: Built on Jetpack `DataStore<Preferences>`, persisting:
  - `activeProviderId` & `selectedModelId`
  - `isFreeModelsOnly` toggle
  - `darkThemeEnabled`
  - `defaultSystemPrompt`
  - `isHighThinking`
- **Startup Sync**: Load DataStore preferences *before* initializing the model selector or sending chat requests.

### B. OpenRouter API & Dynamic Model Fetching Upgrade
- Endpoint: `GET https://openrouter.ai/api/v1/models`
- Header: `Authorization: Bearer ${apiKey}`
- Extract `id` (e.g. `anthropic/claude-3.5-sonnet`), `name`, `context_length`, `pricing`, and modalities.
- Render exact Model ID (`provider/model-name`) underneath the human-readable display name.
- Persist fetched model lists in Room DB; load cached models on cold boot without showing fake model IDs on fetch failures.

### C. Manual Model ID Entry & Validation
- Add a dedicated "Custom Model ID" input field in `ProvidersScreen`.
- Include a **TEST MODEL** button that sends a 1-token minimal test request to verify API compatibility before saving.

### D. Navigation & Lifecycle Fix
- Expose single DataStore-backed Flow state in `ChatViewModel` and `ProvidersViewModel`.
- Ensure async fetch operations check current user selection timestamps so background fetches never overwrite newer manual user selections.

---

## 3. Technical System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      OMNIA MAX Persistence Layer                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌───────────────────────────┐         ┌───────────────────────────┐   │
│   │ Jetpack Preferences       │         │ Keystore Encrypted        │   │
│   │ DataStore                 │         │ SharedPreferences         │   │
│   │ (Selected Model, Theme,   │         │ (OpenAI & OpenRouter      │   │
│   │  Free Models Only Toggle) │         │  Secret API Keys)         │   │
│   └─────────────┬─────────────┘         └─────────────┬─────────────┘   │
│                 │                                     │                 │
│                 └──────────────────┬──────────────────┘                 │
│                                    │                                    │
│                         ┌──────────▼──────────┐                         │
│                         │ AppPreferencesRepo  │                         │
│                         └──────────┬──────────┘                         │
│                                    │                                    │
│                         ┌──────────▼──────────┐                         │
│                         │  Room DB Persistence│                         │
│                         │ (Cached Model Lists,│                         │
│                         │  Chat Messages)     │                         │
│                         └─────────────────────┘                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Test Verification Plan

1. Save OpenRouter API key in Keystore → Verify encrypted storage.
2. Fetch OpenRouter models → Verify exact model IDs (`provider/model-name`) are displayed and stored in Room.
3. Select model & restart app → Verify selected model persists from DataStore.
4. Navigate across screens → Verify active model and theme stay intact.
5. Enter manual Model ID & click Test → Verify real API query validation and persistence.
