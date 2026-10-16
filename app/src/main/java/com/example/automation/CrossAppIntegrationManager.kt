package com.example.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.model.AutomationAction
import com.example.data.model.CrossAppTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CrossAppIntegrationManager(private val context: Context) {

    suspend fun executeAction(
        target: CrossAppTarget,
        action: AutomationAction,
        variables: Map<String, String> = emptyMap()
    ): ActionResult = withContext(Dispatchers.IO) {
        try {
            val evaluatedParams = action.parameters.mapValues { (_, value) ->
                var str = value
                variables.forEach { (k, v) ->
                    str = str.replace("{{$k}}", v)
                }
                str
            }

            when (target) {
                CrossAppTarget.WHATSAPP -> executeWhatsApp(action.actionType, evaluatedParams)
                CrossAppTarget.YOUTUBE -> executeYouTube(action.actionType, evaluatedParams)
                CrossAppTarget.TELEGRAM -> executeTelegram(action.actionType, evaluatedParams)
                CrossAppTarget.INSTAGRAM -> executeInstagram(action.actionType, evaluatedParams)
                CrossAppTarget.SPOTIFY -> executeSpotify(action.actionType, evaluatedParams)
                CrossAppTarget.GMAIL -> executeGmail(action.actionType, evaluatedParams)
                CrossAppTarget.CHROME -> executeChrome(action.actionType, evaluatedParams)
                CrossAppTarget.SYSTEM -> executeSystem(action.actionType, evaluatedParams)
                CrossAppTarget.X_TWITTER -> executeX(action.actionType, evaluatedParams)
                CrossAppTarget.FACEBOOK -> executeFacebook(action.actionType, evaluatedParams)
                CrossAppTarget.CUSTOM_WEBHOOK -> executeWebhook(evaluatedParams)
            }
        } catch (e: Exception) {
            Log.e("CrossAppIntegration", "Failed to execute action ${action.actionType} on $target", e)
            ActionResult(
                success = false,
                outputSummary = "Failed: ${e.localizedMessage ?: "Unknown error"}",
                data = emptyMap()
            )
        }
    }

    private fun executeWhatsApp(actionType: String, params: Map<String, String>): ActionResult {
        val phone = params["phone"] ?: ""
        val message = params["message"] ?: params["text"] ?: ""
        val encodedMsg = URLEncoder.encode(message, "UTF-8")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = if (phone.isNotBlank()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedMsg")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            ActionResult(
                success = true,
                outputSummary = "WhatsApp action dispatched for ${if (phone.isNotBlank()) phone else "contact choice"}",
                data = mapOf("phone" to phone, "message" to message)
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                outputSummary = "WhatsApp app not installed or error: ${e.message}",
                data = emptyMap()
            )
        }
    }

    private fun executeYouTube(actionType: String, params: Map<String, String>): ActionResult {
        val query = params["query"] ?: params["search"] ?: ""
        val videoId = params["videoId"] ?: ""
        val uri = if (videoId.isNotBlank()) {
            Uri.parse("https://www.youtube.com/watch?v=$videoId")
        } else if (query.isNotBlank()) {
            Uri.parse("https://www.youtube.com/results?search_query=${URLEncoder.encode(query, "UTF-8")}")
        } else {
            Uri.parse("https://www.youtube.com")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            ActionResult(
                success = true,
                outputSummary = "Opened YouTube with uri: $uri",
                data = mapOf("uri" to uri.toString())
            )
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Error opening YouTube: ${e.message}", data = emptyMap())
        }
    }

    private fun executeTelegram(actionType: String, params: Map<String, String>): ActionResult {
        val username = params["username"] ?: ""
        val message = params["message"] ?: ""
        val encodedMsg = URLEncoder.encode(message, "UTF-8")
        val uriStr = if (username.isNotBlank()) {
            "https://t.me/$username?text=$encodedMsg"
        } else {
            "https://t.me/share/url?url=&text=$encodedMsg"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "Telegram dispatch executed ($username)", data = mapOf("username" to username))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Telegram not installed or error: ${e.message}", data = emptyMap())
        }
    }

    private fun executeInstagram(actionType: String, params: Map<String, String>): ActionResult {
        val username = params["username"] ?: ""
        val uriStr = if (username.isNotBlank()) {
            "https://instagram.com/_u/$username"
        } else {
            "https://instagram.com"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "Instagram target launched ($uriStr)", data = mapOf("uri" to uriStr))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Instagram launch error: ${e.message}", data = emptyMap())
        }
    }

    private fun executeSpotify(actionType: String, params: Map<String, String>): ActionResult {
        val query = params["query"] ?: params["playlist"] ?: ""
        val uriStr = if (query.isNotBlank()) {
            "spotify:search:${URLEncoder.encode(query, "UTF-8")}"
        } else {
            "https://open.spotify.com"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "Spotify target triggered ($query)", data = mapOf("query" to query))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Spotify error: ${e.message}", data = emptyMap())
        }
    }

    private fun executeGmail(actionType: String, params: Map<String, String>): ActionResult {
        val recipient = params["to"] ?: params["recipient"] ?: ""
        val subject = params["subject"] ?: "Automated Message from OMNIA MAX"
        val body = params["body"] ?: params["message"] ?: ""

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$recipient")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "Gmail draft launched for $recipient", data = mapOf("recipient" to recipient))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Gmail dispatch failed: ${e.message}", data = emptyMap())
        }
    }

    private fun executeChrome(actionType: String, params: Map<String, String>): ActionResult {
        var url = params["url"] ?: "https://google.com"
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "Browser opened URL: $url", data = mapOf("url" to url))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Chrome launch failed: ${e.message}", data = emptyMap())
        }
    }

    private fun executeX(actionType: String, params: Map<String, String>): ActionResult {
        val tweet = params["text"] ?: params["message"] ?: ""
        val url = "https://twitter.com/intent/tweet?text=${URLEncoder.encode(tweet, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "X/Twitter composer launched", data = mapOf("text" to tweet))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "X launch failed: ${e.message}", data = emptyMap())
        }
    }

    private fun executeFacebook(actionType: String, params: Map<String, String>): ActionResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(success = true, outputSummary = "Facebook launched", data = emptyMap())
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "Facebook error: ${e.message}", data = emptyMap())
        }
    }

    private fun executeSystem(actionType: String, params: Map<String, String>): ActionResult {
        val message = params["message"] ?: "Automation executed successfully"
        val label = params["label"] ?: "System Alert"

        // Create share sheet or system action
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "[$label] $message")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, label).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(shareIntent)
            ActionResult(success = true, outputSummary = "System Share Action launched for: $message", data = mapOf("message" to message))
        } catch (e: Exception) {
            ActionResult(success = false, outputSummary = "System action failed: ${e.message}", data = emptyMap())
        }
    }

    private fun executeWebhook(params: Map<String, String>): ActionResult {
        val urlString = params["url"] ?: params["webhookUrl"] ?: return ActionResult(false, "Webhook URL missing", emptyMap())
        val payload = params["payload"] ?: params["data"] ?: "{}"

        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            connection.outputStream.use { os ->
                val input = payload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                ActionResult(
                    success = true,
                    outputSummary = "Webhook delivered HTTP $responseCode to $urlString",
                    data = mapOf("httpCode" to responseCode.toString())
                )
            } else {
                ActionResult(
                    success = false,
                    outputSummary = "Webhook HTTP error $responseCode",
                    data = mapOf("httpCode" to responseCode.toString())
                )
            }
        } catch (e: Exception) {
            ActionResult(
                success = false,
                outputSummary = "Webhook exception: ${e.localizedMessage}",
                data = emptyMap()
            )
        }
    }
}

data class ActionResult(
    val success: Boolean,
    val outputSummary: String,
    val data: Map<String, String>
)
