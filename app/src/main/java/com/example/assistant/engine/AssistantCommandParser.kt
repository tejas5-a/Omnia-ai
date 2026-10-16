package com.example.assistant.engine

import com.example.assistant.model.AssistantCommand
import com.example.assistant.model.MessagingPlatform
import java.util.Locale

object AssistantCommandParser {

    fun parseCommand(input: String): AssistantCommand {
        val trimmed = input.trim().lowercase(Locale.ROOT)

        // 1. Flashlight
        if (trimmed.contains("flashlight") || trimmed.contains("torch")) {
            val enable = !trimmed.contains("off") && !trimmed.contains("disable")
            return AssistantCommand.ToggleFlashlight(enable)
        }

        // 2. Battery status
        if (trimmed.contains("battery") || trimmed.contains("power level") || trimmed.contains("charge level")) {
            return AssistantCommand.GetBatteryStatus
        }

        // 3. Date & Time
        if (trimmed.contains("what time") || trimmed.contains("what date") || trimmed.contains("today's date") || trimmed == "time" || trimmed == "date") {
            return AssistantCommand.GetDateTime
        }

        // 4. Volume Controls
        if (trimmed.contains("volume")) {
            if (trimmed.contains("what") || trimmed.contains("check") || trimmed.contains("get")) {
                return AssistantCommand.GetVolume
            }
            val percentMatch = Regex("(\\d+)\\s*%?").find(trimmed)
            val level = percentMatch?.groupValues?.get(1)?.toIntOrNull() ?: 50
            return AssistantCommand.SetVolume(level.coerceIn(0, 100))
        }
        if (trimmed.contains("mute phone") || trimmed.contains("silence phone")) {
            return AssistantCommand.SetVolume(0)
        }

        // 5. Timer
        if (trimmed.contains("timer")) {
            val minutesMatch = Regex("(\\d+)\\s*(minute|min)").find(trimmed)
            val secondsMatch = Regex("(\\d+)\\s*(second|sec)").find(trimmed)
            val minutes = minutesMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val seconds = secondsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val totalSeconds = if (minutes == 0 && seconds == 0) 300 else (minutes * 60 + seconds)
            return AssistantCommand.CreateTimer(totalSeconds, "MAX Timer")
        }

        // 6. Alarm
        if (trimmed.contains("alarm") || trimmed.contains("wake me up")) {
            val timeMatch = Regex("(\\d{1,2})(:(\\d{2}))?\\s*(am|pm)?").find(trimmed)
            var hour = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 7
            val minute = timeMatch?.groupValues?.get(3)?.toIntOrNull() ?: 0
            val ampm = timeMatch?.groupValues?.get(4)
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return AssistantCommand.CreateAlarm(hour, minute, "MAX Alarm")
        }

        // 7. Settings Intents
        if (trimmed.contains("wifi settings") || trimmed.contains("wi-fi settings")) {
            return AssistantCommand.OpenSettings("wifi")
        }
        if (trimmed.contains("bluetooth settings")) {
            return AssistantCommand.OpenSettings("bluetooth")
        }
        if (trimmed.contains("hotspot settings")) {
            return AssistantCommand.OpenSettings("hotspot")
        }
        if (trimmed.contains("open settings")) {
            return AssistantCommand.OpenSettings("main")
        }

        // 8. Messaging Commands
        // Examples:
        // "Send a WhatsApp message to Rahul saying I'll call later"
        // "Prepare an Instagram DM to Rahul saying Happy Birthday"
        // "Message Rahul on Messenger that I'm running late"
        // "Send an SMS to Rahul saying I'm outside"
        val isMessaging = trimmed.contains("send a message") || trimmed.contains("send message") ||
                trimmed.contains("message ") || trimmed.contains("send an sms") || trimmed.contains("send sms") ||
                trimmed.contains("whatsapp") || trimmed.contains("instagram dm") || trimmed.contains("messenger")

        if (isMessaging) {
            var platform = MessagingPlatform.SMS
            if (trimmed.contains("whatsapp")) platform = MessagingPlatform.WHATSAPP
            else if (trimmed.contains("instagram")) platform = MessagingPlatform.INSTAGRAM
            else if (trimmed.contains("messenger")) platform = MessagingPlatform.MESSENGER

            // Extract recipient and body
            // Pattern: ... to [Recipient] (saying|that) [Message]
            val regexToSaying = Regex("(?:to|message)\\s+([a-zA-Z0-9\\s]+?)\\s+(?:saying|that|with content|say)\\s+(.+)")
            val match = regexToSaying.find(input)

            if (match != null) {
                val recipient = match.groupValues[1].trim()
                val body = match.groupValues[2].trim()
                return AssistantCommand.PrepareMessage(platform, recipient, body)
            } else {
                // Fallback extraction
                val recipientRegex = Regex("(?:to|message)\\s+([a-zA-Z0-9\\s]+)")
                val recipient = recipientRegex.find(input)?.groupValues?.get(1)?.trim() ?: "Contact"
                return AssistantCommand.PrepareMessage(platform, recipient, input)
            }
        }

        // 9. Open Application
        if (trimmed.startsWith("open ") || trimmed.startsWith("launch ")) {
            val appName = input.replaceFirst(Regex("(?i)^(open|launch)\\s+"), "").trim()
            return AssistantCommand.OpenApp(appName)
        }

        // 10. Fallback to OpenRouter General AI
        return AssistantCommand.GeneralAIQuery(input)
    }
}
