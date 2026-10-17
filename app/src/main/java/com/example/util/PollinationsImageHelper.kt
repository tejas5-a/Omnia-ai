package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PollinationsImageHelper {

    private val POLLINATIONS_URL_REGEX = Regex(
        """https?://(?:media|image|gen|text)?\.?pollinations\.ai/[^\s()<>\[\]"'`]+""",
        RegexOption.IGNORE_CASE
    )

    private val GENERAL_IMAGE_URL_REGEX = Regex(
        """https?://[^\s()<>\[\]"'`]+\.(?:jpg|jpeg|png|webp|gif)(?:\?[^\s()<>\[\]"'`]*)?""",
        RegexOption.IGNORE_CASE
    )

    fun extractImageUrls(text: String): List<String> {
        val urls = mutableSetOf<String>()

        // 1. Pollinations AI URLs
        POLLINATIONS_URL_REGEX.findAll(text).forEach { match ->
            val clean = cleanUrl(match.value)
            if (clean.isNotBlank()) {
                urls.add(clean)
            }
        }

        // 2. Markdown Image Syntax ![alt](url)
        val markdownImgRegex = Regex("""!\[.*?\]\((https?://.*?)\)""", RegexOption.IGNORE_CASE)
        markdownImgRegex.findAll(text).forEach { match ->
            val urlGroup = match.groups[1]?.value
            if (!urlGroup.isNullOrBlank()) {
                val clean = cleanUrl(urlGroup)
                if (clean.isNotBlank()) {
                    urls.add(clean)
                }
            }
        }

        // 3. General direct image URLs (.png, .jpg, etc.)
        GENERAL_IMAGE_URL_REGEX.findAll(text).forEach { match ->
            val clean = cleanUrl(match.value)
            if (clean.isNotBlank()) {
                urls.add(clean)
            }
        }

        return urls.toList()
    }

    fun cleanUrl(rawUrl: String): String {
        var url = rawUrl.trim()

        // Strip markdown wrappers like ![...] or [...] if present
        if (url.startsWith("![") && url.contains("](")) {
            val startIndex = url.indexOf("](") + 2
            val endIndex = url.lastIndexOf(")")
            if (endIndex > startIndex) {
                url = url.substring(startIndex, endIndex)
            }
        }

        // Strip trailing punctuation
        while (url.endsWith(".") || url.endsWith(",") || url.endsWith(")") || url.endsWith("]") || url.endsWith(">") || url.endsWith(";")) {
            url = url.substring(0, url.length - 1)
        }

        // Ensure HTTPS protocol
        if (url.startsWith("http://")) {
            url = "https://" + url.removePrefix("http://")
        }

        return url
    }

    fun stripImageUrlsFromText(text: String, extractedUrls: List<String>): String {
        var cleaned = text

        // Remove markdown image blocks
        cleaned = cleaned.replace(Regex("""!\[.*?\]\((https?://.*?)\)"""), "")

        // Remove plain extracted URLs
        for (url in extractedUrls) {
            cleaned = cleaned.replace(url, "")
        }

        return cleaned.trim()
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Image URL") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open URL: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareImageUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                putExtra(Intent.EXTRA_SUBJECT, "Pollinations AI Generated Image")
            }
            context.startActivity(Intent.createChooser(intent, "Share Image Link"))
        } catch (e: Exception) {
            Toast.makeText(context, "Sharing failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
