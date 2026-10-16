package com.example.assistant.model

enum class AssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    CONFIRMATION_REQUIRED,
    SUCCESS,
    ERROR
}

enum class CommandType {
    FLASHLIGHT,
    VOLUME_SET,
    VOLUME_GET,
    ALARM_CREATE,
    TIMER_CREATE,
    BATTERY_STATUS,
    DATE_TIME,
    OPEN_APP,
    OPEN_SETTINGS,
    SEND_MESSAGE,
    GENERAL_AI
}

enum class MessagingPlatform(val displayName: String, val packageName: String?) {
    SMS("SMS", null),
    WHATSAPP("WhatsApp", "com.whatsapp"),
    INSTAGRAM("Instagram", "com.instagram.android"),
    MESSENGER("Messenger", "com.facebook.orca")
}

sealed class AssistantCommand {
    data class ToggleFlashlight(val enable: Boolean) : AssistantCommand()
    data class SetVolume(val levelPercent: Int) : AssistantCommand()
    object GetVolume : AssistantCommand()
    data class CreateAlarm(val hour: Int, val minute: Int, val label: String) : AssistantCommand()
    data class CreateTimer(val durationSeconds: Int, val label: String) : AssistantCommand()
    object GetBatteryStatus : AssistantCommand()
    object GetDateTime : AssistantCommand()
    data class OpenApp(val appName: String) : AssistantCommand()
    data class OpenSettings(val settingsType: String) : AssistantCommand()
    data class PrepareMessage(
        val platform: MessagingPlatform,
        val recipientName: String,
        val messageText: String,
        val languageInstruction: String? = null
    ) : AssistantCommand()
    data class GeneralAIQuery(val prompt: String) : AssistantCommand()
}

data class ResolvedContact(
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null
)

data class CommandExecutionResult(
    val isSuccess: Boolean,
    val message: String,
    val spokenResponse: String,
    val requiresUserAction: Boolean = false
)
