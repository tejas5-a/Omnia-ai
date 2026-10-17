# Implementation Plan — OMNIA MAX: Attachment Navigation, MAX Automations Repair & Scheduled Messaging

Comprehensive architecture and implementation plan for resolving attachment picker state resets, adding granular action deletion with Undo in MAX Automations, and implementing Scheduled Messaging with recipient selection and notification triggers in **OMNIA MAX**.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed by the user during Phase 1 clarification:

- **Confirmed Decision 1 (Scheduled Message Delivery)**: Scheduled messages for social/messaging apps (WhatsApp, Telegram, Instagram, X) will trigger a high-priority system notification at the scheduled time. Tapping the notification opens the target app with pre-filled recipient and content for 1-tap manual confirmation.
- **Confirmed Decision 2 (Action Deletion & Undo)**: Individual action steps in MAX Automations can be deleted directly with a trash icon beside each action step. The list order and persistence update immediately with a 5-second Undo snackbar.
- **Confirmed Decision 3 (Scheduled Messages Screen)**: A dedicated **Scheduled Messages** manager screen will be housed inside the MAX Automations dashboard with options to review, edit, cancel, retry, or delete scheduled messages.

---

## 1. Overview & Core Concept

This update resolves three primary operational issues in OMNIA MAX while maintaining the app's premium dark aesthetic and existing multi-provider AI chat capabilities:

1. **Attachment Navigation Fix**: Fixes unexpected resets to the home screen when selecting images/documents by isolating `ActivityResultLauncher` contracts (`PickVisualMedia`, `OpenDocument`), preserving Compose state, and enforcing persistent `"chat"` screen navigation.
2. **MAX Automations Action Management**: Enhances the visual workflow builder with per-action trash buttons, immediate state persistence, reordering, and a 5-second Undo snackbar.
3. **Scheduled Messaging Engine**: Provides a complete message scheduling subsystem supporting date/time pickers, contact picker integration (`PickContact`), app selection, Android `AlarmManager` / `WorkManager` scheduling, and delivery status tracking (`SCHEDULED`, `AWAITING_USER_ACTION`, `SENT`, `FAILED`, `CANCELLED`).

---

## 2. User Experience & Visual Design

### Key User Flows

```
[Main Chat Screen]
  ├── Tap Attachment Icon -> System File/Image Picker -> Return to Chat (No Home Screen Reset!)
  └── Attachment Preview Card rendered above Composer with processing badge

[MAX Automations Dashboard]
  ├── Action Steps List -> Tap Trash Icon beside individual step -> Immediate removal + Undo Snackbar
  ├── Tap "Delete Workflow" -> Confirmation Dialog -> Workflow deleted from Room DB
  └── Tap "Scheduled Messages" -> Open Scheduled Messages Management Screen

[Scheduled Messages Screen]
  ├── Floating Action Button (+) -> Schedule New Message Dialog
  │     ├── Select App (WhatsApp, Telegram, Instagram, X, Gmail)
  │     ├── Select Recipient (System Contact Picker or Manual Input)
  │     ├── Set Date & Time (DatePicker & TimePicker)
  │     └── Enter Message Content
  └── Review Scheduled Item Cards (Status, App Icon, Recipient, Time) -> Actions: Edit / Cancel / Retry / Delete
```

### Visual Identity & Theme
- **Surface Palette**: `#121212` main background, `#171B27` secondary, `#202635` cards, `#282F40` elevated surfaces.
- **Accents**: `#3478F6` primary blue, `#60A5FA` secondary cyan, `#34D399` success green, `#F87171` error red, `#F59E0B` warning amber.
- **Typography**: Clean `sp` scaled Material 3 typography with bold header hierarchy and monospace code snippets.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Attachment Picker Navigation Isolation
- **Chosen Approach**: Retain Compose state using `rememberSaveable` and ensure `appPrefs.updateLastScreen("chat")` is called whenever `ChatScreen` is mounted, so Activity lifecycle pauses caused by system file pickers do not cause `MainActivity` to re-evaluate `lastScreen` as `"home"`.
- **Why**: Eliminates the jarring jump to the home screen when selecting attachments.

### Decision 2: Scheduled Delivery via AlarmManager + Notifications
- **Chosen Approach**: Use Android `AlarmManager` (`setExactAndAllowWhileIdle`) + `BroadcastReceiver` + System Notification for exact timing across app/device restarts. When alarm triggers, a notification is posted. Tapping the notification launches the target app's intent with pre-filled recipient and text.
- **Why**: Modern Android OS restrictions prohibit third-party apps from sending messages silently in background for apps like WhatsApp/Instagram. Pre-filling and prompting the user satisfies Android security policies while delivering 1-tap dispatch.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            UI LAYER (JETPACK COMPOSE)                       │
├──────────────────────────────┬───────────────────────────────┬──────────────┤
│          ChatScreen          │    AutomationsDashboardScreen │ Scheduled    │
│    (Inline Attachments)      │  (Workflow & Action Builder)  │ Messages UI  │
└──────────────┬───────────────┴───────────────┬───────────────┴──────┬───────┘
               │                               │                      │
               ▼                               ▼                      ▼
┌──────────────────────────────┬──────────────────────────────────────────────┐
│       ChatViewModel          │             AutomationsViewModel             │
└──────────────┬───────────────┴───────────────────────┬──────────────────────┘
               │                                       │
               ▼                                       ▼
┌──────────────────────────────┬──────────────────────────────────────────────┐
│        ChatRepository        │             AutomationRepository             │
├──────────────────────────────┴──────────────────────────────────────────────┤
│                             ROOM DATABASE (SQLITE)                          │
│   chat_threads | chat_messages | automations | scheduled_messages | logs    │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ANDROID SYSTEM SERVICE INTEGRATION                     │
│    AlarmManager | BroadcastReceiver | ContactPicker | StorageAccessFramework│
└─────────────────────────────────────────────────────────────────────────────┘
```

### New / Updated Data Entities

1. **`ScheduledMessageEntity` (Room Database)**:
   - `id: String` (UUID)
   - `targetApp: String` (WHATSAPP, TELEGRAM, INSTAGRAM, X, GMAIL, etc.)
   - `recipientName: String`
   - `recipientContactInfo: String` (Phone number / handle / email)
   - `messageContent: String`
   - `scheduledEpochMs: Long`
   - `status: String` (`SCHEDULED`, `AWAITING_USER_ACTION`, `SENT`, `FAILED`, `CANCELLED`)
   - `createdAt: Long`

2. **`ScheduledMessageDao`**:
   - `getAllScheduledMessages(): Flow<List<ScheduledMessageEntity>>`
   - `insertScheduledMessage(item: ScheduledMessageEntity)`
   - `updateStatus(id: String, status: String)`
   - `deleteScheduledMessage(id: String)`

---
