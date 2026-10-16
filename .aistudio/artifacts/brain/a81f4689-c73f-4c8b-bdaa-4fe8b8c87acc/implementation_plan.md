# MAX AUTOMATIONS — Ultimate Cross-App Automation System

Upgrade OMNIA MAX with **MAX AUTOMATIONS**, a production-oriented cross-app automation platform allowing users to create, schedule, customize, and execute multi-step workflows across popular Android applications (WhatsApp, Instagram, YouTube, Telegram, X, Facebook, Gmail, Chrome, Spotify) and external integrations, wrapped in OMNIA MAX's signature sci-fi UI identity.

> [!IMPORTANT]
> **Strict AI Assistant Removal Preservation**: As mandated, the AI assistant functionality remains completely removed. MAX AUTOMATIONS is purely a deterministic, user-controlled cross-app workflow automation engine using Android intents, deep links, share sheet delegates, background scheduling, webhooks, and official APIs.

---

## User Review & Critical Decisions

- **Confirmed Decision 1 (Triggers)**: Hybrid trigger execution model supporting **Manual execution**, **Scheduled time/date**, **System events** (battery, connectivity), and **Webhooks**.
- **Confirmed Decision 2 (App Integrations)**: Direct app actions via Android Intents, Deep Links, and Android Share Sheet as default; official APIs as advanced options; explicit user confirmation as safeguard for consequential actions.
- **Confirmed Decision 3 (Dashboard Aesthetic)**: OMNIA MAX sci-fi dark posture (deep navy `#0B0F19`, glass cards with subtle glowing borders, cyan `#00F2FE` and electric violet `#7F00FF` accents).

---

## 1. Overview & Core Concept

**MAX AUTOMATIONS** delivers cross-app automation for Android power users:
- **Visual Workflow Builder**: Structured `WHEN -> IF -> THEN -> REVIEW -> SAVE` flow for creating deterministic rules.
- **Universal App Integrations**: Native Intent handlers, deep-link triggers, share sheet preparers, and official API integration adapters for WhatsApp, YouTube, Telegram, Instagram, X, Facebook, Gmail, Chrome, Spotify, and generic Android apps.
- **Robust Background Execution**: Time-zone aware scheduling via `WorkManager` and `AlarmManager` with Doze-mode compatibility and idempotency execution tracking.
- **Integrations & Webhook Center**: Configuration hub for Webhook endpoints, OAuth tokens, and API credential management.

---

## 2. User Experience & Visual Design

### Dashboard Layout & Navigation
- **Header Stat Cards**: Total Automations, Active/Paused count, Execution Success Rate, Last Triggered timestamp.
- **Filter & Search Bar**: Search by workflow name, filter by status (`All`, `Active`, `Scheduled`, `Paused`) or target app (`WhatsApp`, `YouTube`, `Telegram`, etc.), and sort by date or execution count.
- **Workflow Cards**: Glassmorphism cards featuring app icons, trigger badge, action chain overview, enabled toggle switch, execution status pill, and quick action menu (`Edit`, `Run Now`, `Duplicate`, `Export`, `Delete`).
- **Template Gallery**: Pre-built one-tap templates (Daily Reminder, YouTube Routine, Study Focus, Social Share, Prepared Email Draft, Webhook Bridge).

### Visual Workflow Builder (`WHEN -> IF -> THEN -> REVIEW -> SAVE`)
```
┌────────────────────────────────────────────────────────────────────────┐
│                        VISUAL WORKFLOW BUILDER                        │
├───────────────┬───────────────┬───────────────┬───────────────┬────────┤
│    1. WHEN    │    2. IF      │    3. THEN    │   4. REVIEW   │ 5. SAVE│
│   (Trigger)   │  (Condition)  │   (Actions)   │   (Confirm)   │ (Done) │
└───────┬───────┴───────┬───────┴───────┬───────┴───────┬───────┴────────┘
        │               │               │               │
        ▼               ▼               ▼               ▼
┌──────────────┐┌──────────────┐┌──────────────┐┌──────────────┐
│• Manual Tap  ││• Time/Day    ││• Open App    ││• Permission  │
│• Scheduled   ││• Battery %   ││• Deep Link   ││  Validation │
│• System Event││• Wi-Fi State ││• Share Text  ││• Safeguard   │
│• Webhook     ││• App State   ││• Compose Msg ││  Prompt Check│
└──────────────┘└──────────────┘└──────────────┘└──────────────┘
```

---

## 3. Technical Architecture & Component Hierarchy

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           UI LAYER (Jetpack Compose)                            │
├───────────────────────────────┬─────────────────────────────────────────────────┤
│ AutomationsDashboardScreen    │ VisualWorkflowBuilderScreen                     │
│ IntegrationsCenterScreen      │ ExecutionHistoryScreen                          │
└───────────────┬───────────────┴────────────────────────┬────────────────────────┘
                │                                        │
                ▼                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          VIEWMODEL & ENGINE LAYER                               │
├───────────────────────────────┬─────────────────────────────────────────────────┤
│ AutomationsViewModel          │ AutomationExecutionEngine                       │
│ TriggerRegistry               │ ConditionEvaluator                              │
│ ActionRegistry                │ AppIntegrationFramework                         │
└───────────────┬───────────────┴────────────────────────┬────────────────────────┘
                │                                        │
                ▼                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    PERSISTENCE & SCHEDULING DATA LAYER                          │
├───────────────────────────────┬─────────────────────────────────────────────────┤
│ Room AppDatabase (Workflows)  │ WorkManager Scheduler                           │
│ SecurePreferencesManager      │ Intent & DeepLink Dispatcher                    │
└───────────────────────────────┴─────────────────────────────────────────────────┘
```

### Data Models & Room Schema

1. **`AutomationEntity`** (`automations` table):
   - `id`: String (UUID)
   - `name`: String
   - `description`: String
   - `isEnabled`: Boolean
   - `triggerJson`: String (Serialized `AutomationTrigger`)
   - `conditionsJson`: String (Serialized `List<AutomationCondition>`)
   - `actionsJson`: String (Serialized `List<AutomationAction>`)
   - `targetApp`: String (WhatsApp, YouTube, Telegram, Spotify, etc.)
   - `createdAt`: Long
   - `lastRunAt`: Long
   - `lastRunStatus`: String (`SUCCESS`, `FAILED`, `SKIPPED`, `WAITING_FOR_USER`, `PERMISSION_REQUIRED`)
   - `runCount`: Int

2. **`AutomationRunLogEntity`** (`automation_run_logs` table):
   - `id`: String
   - `automationId`: String
   - `automationName`: String
   - `status`: String
   - `executionSummary`: String
   - `timestamp`: Long

---

## 4. Universal App Integration Matrix

| Target Application | Trigger Capabilities | Action Capabilities & System Mechanics | Safeguards & Limitations |
| :--- | :--- | :--- | :--- |
| **WhatsApp** | Manual, Scheduled, Webhook | Open Chat Link, Prepare Message, Share Text/Media via Intent | Requires user confirmation to tap Send in WhatsApp. |
| **YouTube** | Manual, Scheduled Time | Open Video URL, Open Playlist, Watch Later Reminder | Launches official YouTube app/URL. Respects playback limits. |
| **Telegram** | Manual, Scheduled, Webhook | Open Channel/Chat, Share Text, Official Bot API sendMessage | Tokens stored in SecurePreferencesManager. |
| **Instagram** | Manual, Scheduled | Open Profile/Reel, Share to Feed/Stories via Share Sheet | Media prepped via Share Sheet. |
| **X (Twitter)** | Manual, Scheduled, Webhook | Open Profile/Post, Compose Tweet Intent, Share Sheet | Intent opens compose box. |
| **Facebook** | Manual, Scheduled | Open Page/Group, Share Content via Intent | Uses Android Intent delegates. |
| **Gmail** | Manual, Scheduled | Compose Draft (`ACTION_SENDTO` / `ACTION_SEND`), Subject & Body | User reviews and sends in Gmail. |
| **Chrome / Web** | Manual, Scheduled, Webhook | Open Validated URL, Webhook Endpoint Receiver | URL scheme validation (HTTPS/HTTP). |
| **Spotify** | Manual, Scheduled | Open Track/Album/Playlist Deep Link (`spotify:track:...`) | Deep link opens Spotify. |
| **Generic Apps** | Manual, Scheduled | Package Launch Intent, Share Sheet, In-App Screen | Checks package installation status first. |

---

## 5. Implementation Phasing & Milestones

1. **Phase 1: Architecture & Data Layer**:
   - Create models (`AutomationModels.kt`, `AutomationEntity`, `AutomationRunLogEntity`).
   - Add DAO and Room database migration.
   - Implement `AutomationRepository`.

2. **Phase 2: App Integration Handlers**:
   - Create `AppIntegrationFramework.kt` and individual integration drivers (`WhatsAppDriver`, `YouTubeDriver`, `TelegramDriver`, `GmailDriver`, etc.).
   - Package visibility checks (`packageManager.getLaunchIntentForPackage`).

3. **Phase 3: Execution Engine & Scheduler**:
   - Create `AutomationExecutionEngine.kt`, `ConditionEvaluator.kt`, and `WorkManager` scheduler task.
   - Implement idempotency checks and execution log reporting.

4. **Phase 4: Dashboard & Visual Builder UI**:
   - Implement `AutomationsDashboardScreen.kt`, `VisualWorkflowBuilderScreen.kt`, and `IntegrationsCenterScreen.kt`.
   - Add template gallery and JSON export/import handlers.

5. **Phase 5: Navigation & MainActivity Integration**:
   - Add `Automations` to sidebar drawer (`DrawerContent.kt`) and `MainActivity` routes.
   - Run compilation checks (`compile_applet`).
