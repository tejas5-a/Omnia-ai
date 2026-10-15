# OMNIA MAX — Mobile AI Agent Operating System

OMNIA MAX is a production-oriented, mobile-first AI Operating System built as a native Android application using Jetpack Compose and Material Design 3. Inspired by Claude's minimal warm design aesthetics and ChatGPT's conversational power, OMNIA MAX integrates a dynamic Multi-Provider API Hub, real-time streaming AI chat, an interactive Multi-Agent Engine (6 specialized agents), and offline local persistence.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following defaults and architectural choices are pre-configured to ensure immediate out-of-the-box usability with zero setup required, while supporting full user customizability.

- **Out-of-the-Box AI Engine**: Uses Google Gemini REST API (defaulting to `gemini-3.6-flash` and `gemini-2.5-flash`) initialized with pre-configured AI Studio keys via `BuildConfig.GEMINI_API_KEY`.
- **API Key Storage & Encryption**: Stored in local Encrypted Shared Preferences / Android Keystore, with fallback to `.env` / `BuildConfig`.
- **Provider Connectivity**: Real live model fetching against Google Gemini (`/v1beta/models`), OpenAI (`/v1/models`), OpenRouter (`/v1/models`), and Custom OpenAI-Compatible Endpoints (`{baseUrl}/models`).
- **Multi-Agent Coordinator**: Executes tasks across 6 specialized agents (Research, Coding, Testing, Writing, Security, Review) with real-time step monitoring, sub-task status updates, and interactive thought logs.
- **Data Persistence**: Local Room SQLite Database for chat threads, messages, provider configurations, custom system prompts, and multi-agent execution logs.

---

## 1. Overview & Core Concept

OMNIA MAX transforms mobile AI from a simple single-turn chatbot into an **Agentic Workspace**:
- **Claude-Inspired Experience**: Warm neutral palette (warm sand, dark slate), subtle typography hierarchy, rounded surfaces, smooth spring micro-animations, edge-to-edge padding.
- **Provider Hub**: Add, test, fetch models, and toggle between Google Gemini, OpenAI, OpenRouter, and local or custom LLM server endpoints (e.g., Ollama, vLLM, LM Studio).
- **OMNIA Multi-Agent Pipeline**: Submit complex prompts (e.g. "Build a secure Kotlin authentication module with tests"), watch the Coordinator break down tasks, delegate to specialized sub-agents in parallel or sequence, and deliver an aggregated report.

---

## 2. User Experience & Visual Design

### Aesthetic Direction
- **Warm Minimalist**: Soft dark slate (`#18181B`) & warm alabaster (`#FBF9F5`) backgrounds, warm amber accents (`#D97706` / `#F59E0B`), soft charcoal cards (`#27272A`), clean border strokes.
- **Typography**: Clean display sans typography for headers, mono-spaced font blocks for code and logs.
- **Safe Areas & Insets**: Edge-to-edge support (`enableEdgeToEdge`), status bar and `WindowInsets.navigationBars` padding across all composables.

### Key Screens & Navigation
1. **Sidebar Drawer (`ModalNavigationDrawer`)**:
   - New Chat button, recent chat threads grouped by date (Today, Yesterday, 7 Days), search input, Agents Dashboard link, Provider Hub link, Settings link.
2. **AI Chat Screen**:
   - Header with Model Selector dropdown, Provider status pill, System Instruction sheet toggle, thinking mode toggle.
   - Message feed supporting streaming text chunks, rendered Markdown, syntax code blocks with copy buttons, image/doc thumbnails, message edit & retry buttons.
   - Bottom floating input container with attachment picker, voice input placeholder, stop generation / send button.
3. **API Provider Hub Screen**:
   - Cards for Google Gemini, OpenAI, OpenRouter, and Custom Endpoint.
   - "FETCH MODELS" button with live API loading spinner, success checkmark, model count pill, and model drop-down list.
   - Connection status test indicator (Latency in ms, HTTP Status).
4. **OMNIA Multi-Agent Dashboard**:
   - Active tasks, pipeline visualizer showing 6 agents (Research, Coding, Testing, Writing, Security, Review).
   - Agent activity status (Idle, Thinking, Executing, Done, Error).
   - Interactive agent thought logs and aggregated final outputs.
5. **Settings & Onboarding Screen**:
   - Theme toggle (Light/Dark/System), Default System Instructions, Export All Chats (JSON), Key Management, Clear Data.

---

## 3. Key Product Decisions & Trade-Offs

- **Direct REST & SSE Streaming**: Implemented via Retrofit + OkHttp with `alt=sse` parsing for Gemini, OpenAI, and OpenRouter streams, avoiding heavy native C++ wrappers while ensuring ultra-fast cold starts.
- **Room Database for Offline First**: Local-first storage guarantees zero data loss on network disconnects.
- **Agent Execution Architecture**: Sequential and parallel Kotlin Coroutine `Flow` pipeline with bounded retries (max 2) and structured output JSON parsing.

---

## 4. Technical Architecture & Component Structure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          OMNIA MAX Android App                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────┐  │
│  │   Jetpack Compose UI  │   │  ModalNavDrawer UI    │   │  Theme &      │  │
│  │ (Chat, Provider Hub,  │   │ (Chat History, Search,│   │  Typography   │  │
│  │  Agent Dashboard)     │   │  Navigation Links)    │   │ (M3 Palette)  │  │
│  └───────────┬───────────┘   └───────────┬───────────┘   └───────┬───────┘  │
│              │                           │                       │          │
│              └─────────────────┬─────────┴───────────────────────┘          │
│                                │                                            │
│                     ┌──────────▼──────────┐                                 │
│                     │ ViewModels & State  │                                 │
│                     │ (Chat, Provider,    │                                 │
│                     │  Agent Engine)      │                                 │
│                     └──────────┬──────────┘                                 │
│                                │                                            │
│        ┌───────────────────────┼───────────────────────┐                    │
│        │                       │                       │                    │
│ ┌──────▼────────┐     ┌────────▼────────┐     ┌────────▼────────┐           │
│ │ Provider Hub  │     │  Agent Engine   │     │ Chat Repository │           │
│ │ Service Adap- │     │ Coordinator &   │     │ & Room DB       │           │
│ │ ters & Models │     │ 6 Agents        │     │ Persistence     │           │
│ └──────┬────────┘     └────────┬────────┘     └────────┬────────┘           │
│        │                       │                       │                    │
└────────┼───────────────────────┼───────────────────────┼────────────────────┘
         │                       │                       │
 ┌───────▼───────┐       ┌───────▼───────┐       ┌───────▼───────┐
 │ Gemini API /  │       │ OpenAI /      │       │ OpenRouter /  │
 │ REST Service  │       │ Custom API    │       │ Local Models  │
 └───────────────┘       └───────────────┘       └───────────────┘
```

### Data Layer Schema (Room SQLite)
- **`ChatThread`**: `id` (UUID), `title`, `createdAt`, `updatedAt`, `modelId`, `providerId`, `systemPrompt`, `pinned`
- **`ChatMessage`**: `id` (UUID), `threadId`, `sender` ("user" | "assistant" | "agent"), `agentName`, `content`, `timestamp`, `imageUris`, `status`
- **`ProviderConfig`**: `id`, `name`, `type` ("GEMINI" | "OPENAI" | "OPENROUTER" | "CUSTOM"), `apiKey`, `baseUrl`, `selectedModel`, `enabled`, `isFetched`
- **`AgentTaskRun`**: `id`, `prompt`, `status`, `summary`, `timestamp`, `logsJson`

---

## 5. Implementation Roadmap & Milestones

1. **Phase 1: Project Setup & Dependencies**:
   - Add required serialization and Room dependencies to `build.gradle.kts`.
   - Setup `Theme.kt`, `Color.kt`, and M3 warm palette.
2. **Phase 2: Database & Data Repositories**:
   - Create Room Entities, DAOs, Database, and Repositories for Chat, Providers, and Agent Tasks.
   - Seed default provider configurations (Gemini, OpenAI, OpenRouter, Custom).
3. **Phase 3: Multi-Provider Hub & Dynamic Model Fetcher**:
   - Implement OkHttp/Retrofit HTTP client adapters for fetching models across Gemini, OpenAI, OpenRouter, and Custom OpenAI endpoints.
   - Build Provider Hub Compose UI with test connections, fetch models, and model pickers.
4. **Phase 4: Streaming Chat Engine & UI**:
   - Build SSE streaming parser for live AI responses.
   - Implement Chat Screen with Markdown support, code block copying, image attachments, retry, regenerate, and system prompt editing.
5. **Phase 5: OMNIA Multi-Agent Engine**:
   - Build Coordinator and 6 Agent runner routines (Research, Coding, Testing, Writing, Security, Review).
   - Build Agent Dashboard UI with real-time status, timeline, and execution history.
6. **Phase 6: Navigation, History Drawer, Search & Settings**:
   - Implement Navigation Drawer, Chat Search, Conversation Export, Theme Switcher, and Onboarding.
7. **Phase 7: Verification & Build**:
   - Run `compile_applet` to verify compilation and error-free execution.
