package com.example.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
enum class TriggerType(val displayName: String, val description: String) {
    MANUAL("Manual Execution", "Triggered on-demand by user tap"),
    SCHEDULED("Scheduled Time", "Triggers at specific time or repeating schedule"),
    SYSTEM_EVENT("System Event", "Triggers on battery, Wi-Fi, or system events"),
    WEBHOOK("Incoming Webhook", "Triggers when HTTP webhook endpoint is invoked")
}

@Keep
enum class CrossAppTarget(val displayName: String, val packageName: String) {
    WHATSAPP("WhatsApp", "com.whatsapp"),
    INSTAGRAM("Instagram", "com.instagram.android"),
    YOUTUBE("YouTube", "com.google.android.youtube"),
    TELEGRAM("Telegram", "org.telegram.messenger"),
    X_TWITTER("X (Twitter)", "com.twitter.android"),
    FACEBOOK("Facebook", "com.facebook.katana"),
    GMAIL("Gmail", "com.google.android.gm"),
    CHROME("Chrome", "com.android.chrome"),
    SPOTIFY("Spotify", "com.spotify.music"),
    CUSTOM_WEBHOOK("Webhook API", "net.webhook"),
    SYSTEM("System Share / Intent", "android.system")
}

@Keep
enum class ExecutionStatus(val displayName: String) {
    SUCCESS("Success"),
    FAILED("Failed"),
    SKIPPED("Skipped"),
    RUNNING("Running")
}

@Keep
data class TriggerConfig(
    val time: String = "09:00",
    val daysOfWeek: List<String> = listOf("MON", "TUE", "WED", "THU", "FRI"),
    val webhookUrl: String = "",
    val eventType: String = "BATTERY_LOW"
)

@Keep
data class AutomationCondition(
    val id: String = UUID.randomUUID().toString(),
    val field: String = "status",
    val operator: String = "EQUALS", // EQUALS, NOT_EQUALS, CONTAINS, GREATER_THAN, LESS_THAN
    val targetValue: String = "active"
)

@Keep
data class AutomationAction(
    val id: String = UUID.randomUUID().toString(),
    val actionType: String = "OPEN_APP",
    val targetApp: CrossAppTarget = CrossAppTarget.SYSTEM,
    val parameters: Map<String, String> = emptyMap()
)

@Keep
data class AutomationWorkflow(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val iconName: String = "SYSTEM",
    val isEnabled: Boolean = true,
    val triggerType: TriggerType = TriggerType.MANUAL,
    val triggerConfig: TriggerConfig = TriggerConfig(),
    val conditions: List<AutomationCondition> = emptyList(),
    val actions: List<AutomationAction> = emptyList(),
    val targetApp: CrossAppTarget = CrossAppTarget.SYSTEM,
    val lastRunMs: Long = 0L,
    val runCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Keep
data class StepExecutionResult(
    val stepId: String,
    val stepName: String,
    val success: Boolean,
    val output: String,
    val durationMs: Long
)

@Keep
data class AutomationRunLog(
    val id: String = UUID.randomUUID().toString(),
    val automationId: String,
    val automationName: String,
    val status: ExecutionStatus,
    val stepResults: List<StepExecutionResult> = emptyList(),
    val outputSummary: String = "",
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class AutomationTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val workflow: AutomationWorkflow
)

object AutomationTemplates {
    val ALL_TEMPLATES = listOf(
        AutomationTemplate(
            id = "whatsapp_morning_update",
            title = "Morning WhatsApp Blast",
            category = "Messaging",
            description = "Pre-fills a daily WhatsApp update to a designated contact every morning.",
            workflow = AutomationWorkflow(
                id = "tpl_whatsapp_1",
                name = "Morning WhatsApp Blast",
                description = "Automated WhatsApp dispatch for morning updates",
                iconName = "WHATSAPP",
                isEnabled = true,
                triggerType = TriggerType.SCHEDULED,
                triggerConfig = TriggerConfig(time = "08:30"),
                conditions = emptyList(),
                actions = listOf(
                    AutomationAction(
                        actionType = "SEND_WHATSAPP",
                        targetApp = CrossAppTarget.WHATSAPP,
                        parameters = mapOf(
                            "phone" to "+1234567890",
                            "message" to "Good morning! Here is today's status update from OMNIA MAX."
                        )
                    )
                ),
                targetApp = CrossAppTarget.WHATSAPP
            )
        ),
        AutomationTemplate(
            id = "youtube_notifier",
            title = "YouTube Video Search & Launch",
            category = "Media",
            description = "Searches YouTube and opens target tech/news playlist automatically.",
            workflow = AutomationWorkflow(
                id = "tpl_youtube_1",
                name = "YouTube Video Search & Launch",
                description = "Open YouTube search for tech news daily",
                iconName = "YOUTUBE",
                isEnabled = true,
                triggerType = TriggerType.MANUAL,
                triggerConfig = TriggerConfig(),
                conditions = emptyList(),
                actions = listOf(
                    AutomationAction(
                        actionType = "SEARCH_YOUTUBE",
                        targetApp = CrossAppTarget.YOUTUBE,
                        parameters = mapOf(
                            "query" to "AI tech news breakdown"
                        )
                    )
                ),
                targetApp = CrossAppTarget.YOUTUBE
            )
        ),
        AutomationTemplate(
            id = "spotify_workout_flow",
            title = "Spotify Fitness Playlist Auto Launch",
            category = "Entertainment",
            description = "Triggers high-energy gym playlist on Spotify with one tap or schedule.",
            workflow = AutomationWorkflow(
                id = "tpl_spotify_1",
                name = "Spotify Fitness Playlist",
                description = "Launch Spotify workout tunes automatically",
                iconName = "SPOTIFY",
                isEnabled = true,
                triggerType = TriggerType.MANUAL,
                triggerConfig = TriggerConfig(),
                conditions = emptyList(),
                actions = listOf(
                    AutomationAction(
                        actionType = "OPEN_PLAYLIST",
                        targetApp = CrossAppTarget.SPOTIFY,
                        parameters = mapOf(
                            "query" to "Workout EDM Beast Mode"
                        )
                    )
                ),
                targetApp = CrossAppTarget.SPOTIFY
            )
        ),
        AutomationTemplate(
            id = "social_x_compose",
            title = "Cross-Post Announcement to X",
            category = "Social Media",
            description = "Opens X post composer pre-populated with customized announcement copy.",
            workflow = AutomationWorkflow(
                id = "tpl_x_1",
                name = "Cross-Post Announcement to X",
                description = "Pre-fill X tweet with project updates",
                iconName = "X_TWITTER",
                isEnabled = true,
                triggerType = TriggerType.MANUAL,
                triggerConfig = TriggerConfig(),
                conditions = emptyList(),
                actions = listOf(
                    AutomationAction(
                        actionType = "COMPOSE_TWEET",
                        targetApp = CrossAppTarget.X_TWITTER,
                        parameters = mapOf(
                            "text" to "Exciting updates released on OMNIA MAX! Check out our cross-app automation suite."
                        )
                    )
                ),
                targetApp = CrossAppTarget.X_TWITTER
            )
        ),
        AutomationTemplate(
            id = "webhook_ingest_system",
            title = "Webhook Endpoint Dispatcher",
            category = "Developer",
            description = "Receives webhooks and forwards system alerts via Android Share Sheet.",
            workflow = AutomationWorkflow(
                id = "tpl_webhook_1",
                name = "Webhook Endpoint Dispatcher",
                description = "Post payloads to external endpoints or system",
                iconName = "CUSTOM_WEBHOOK",
                isEnabled = true,
                triggerType = TriggerType.WEBHOOK,
                triggerConfig = TriggerConfig(webhookUrl = "https://api.omnia.com/v1/alert"),
                conditions = emptyList(),
                actions = listOf(
                    AutomationAction(
                        actionType = "HTTP_POST",
                        targetApp = CrossAppTarget.CUSTOM_WEBHOOK,
                        parameters = mapOf(
                            "url" to "https://httpbin.org/post",
                            "payload" to "{\"status\":\"success\", \"agent\":\"OMNIA MAX\"}"
                        )
                    )
                ),
                targetApp = CrossAppTarget.CUSTOM_WEBHOOK
            )
        )
    )
}
