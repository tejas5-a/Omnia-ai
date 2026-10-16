package com.example.assistant.messaging

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.assistant.model.CommandExecutionResult
import com.example.assistant.model.MessagingPlatform
import com.example.assistant.model.ResolvedContact

class MessagingDispatcher(private val context: Context) {

    fun isPlatformInstalled(platform: MessagingPlatform): Boolean {
        if (platform == MessagingPlatform.SMS) return true
        val packageName = platform.packageName ?: return false
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun dispatchMessage(
        platform: MessagingPlatform,
        contact: ResolvedContact,
        messageText: String
    ): CommandExecutionResult {
        if (!isPlatformInstalled(platform)) {
            return CommandExecutionResult(
                isSuccess = false,
                message = "${platform.displayName} is not installed on this device.",
                spokenResponse = "I couldn't find ${platform.displayName} installed on your phone."
            )
        }

        return when (platform) {
            MessagingPlatform.SMS -> sendSms(contact, messageText)
            MessagingPlatform.WHATSAPP -> sendWhatsApp(contact, messageText)
            MessagingPlatform.INSTAGRAM -> sendInstagram(contact, messageText)
            MessagingPlatform.MESSENGER -> sendMessenger(contact, messageText)
        }
    }

    private fun sendSms(contact: ResolvedContact, messageText: String): CommandExecutionResult {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${contact.phoneNumber ?: ""}")
            putExtra("sms_body", messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandExecutionResult(
                isSuccess = true,
                message = "Opened Messaging app with pre-filled SMS to ${contact.name}.",
                spokenResponse = "Opening SMS to ${contact.name} with your message."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Failed to open SMS app: ${e.localizedMessage}",
                spokenResponse = "Could not open messaging application."
            )
        }
    }

    private fun sendWhatsApp(contact: ResolvedContact, messageText: String): CommandExecutionResult {
        val phoneNum = contact.phoneNumber?.replace(Regex("[^0-9+]"), "") ?: ""
        val url = if (phoneNum.isNotEmpty()) {
            "https://api.whatsapp.com/send?phone=$phoneNum&text=${Uri.encode(messageText)}"
        } else {
            "https://api.whatsapp.com/send?text=${Uri.encode(messageText)}"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandExecutionResult(
                isSuccess = true,
                message = "Opened WhatsApp chat with ${contact.name}.",
                spokenResponse = "Opening WhatsApp to send your message to ${contact.name}."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Failed to launch WhatsApp.",
                spokenResponse = "WhatsApp isn't responding right now."
            )
        }
    }

    private fun sendInstagram(contact: ResolvedContact, messageText: String): CommandExecutionResult {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, messageText)
            setPackage("com.instagram.android")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandExecutionResult(
                isSuccess = true,
                message = "Prepared Instagram DM sharing interface.",
                spokenResponse = "Opening Instagram DM to send your message to ${contact.name}."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Instagram DM dispatch unavailable.",
                spokenResponse = "Could not open Instagram messaging interface."
            )
        }
    }

    private fun sendMessenger(contact: ResolvedContact, messageText: String): CommandExecutionResult {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, messageText)
            setPackage("com.facebook.orca")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandExecutionResult(
                isSuccess = true,
                message = "Prepared Messenger message interface.",
                spokenResponse = "Opening Messenger for ${contact.name}."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Messenger app launcher failed.",
                spokenResponse = "Failed to open Messenger."
            )
        }
    }
}
