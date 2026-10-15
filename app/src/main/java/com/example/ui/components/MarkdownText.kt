package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val blocks = parseMarkdownBlocks(text)

    Column(modifier = modifier) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        code = block.code,
                        language = block.language
                    )
                }
                is MarkdownBlock.TextBlock -> {
                    val annotated = parseFormattedText(block.content)
                    Text(
                        text = annotated,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class TextBlock(val content: String) : MarkdownBlock()
    data class CodeBlock(val code: String, val language: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.split("\n")
    var inCodeBlock = false
    var currentLanguage = ""
    val codeBuffer = StringBuilder()
    val textBuffer = StringBuilder()

    for (line in lines) {
        if (line.trimStart().startsWith("```")) {
            if (inCodeBlock) {
                // Closing code block
                blocks.add(MarkdownBlock.CodeBlock(codeBuffer.toString(), currentLanguage))
                codeBuffer.clear()
                inCodeBlock = false
            } else {
                // Opening code block
                if (textBuffer.isNotEmpty()) {
                    blocks.add(MarkdownBlock.TextBlock(textBuffer.toString().trimEnd()))
                    textBuffer.clear()
                }
                currentLanguage = line.trimStart().removePrefix("```").trim()
                if (currentLanguage.isEmpty()) currentLanguage = "code"
                inCodeBlock = true
            }
        } else {
            if (inCodeBlock) {
                codeBuffer.append(line).append("\n")
            } else {
                textBuffer.append(line).append("\n")
            }
        }
    }

    if (inCodeBlock) {
        blocks.add(MarkdownBlock.CodeBlock(codeBuffer.toString(), currentLanguage))
    } else if (textBuffer.isNotEmpty()) {
        blocks.add(MarkdownBlock.TextBlock(textBuffer.toString().trimEnd()))
    }

    return if (blocks.isEmpty()) listOf(MarkdownBlock.TextBlock(rawText)) else blocks
}

private fun parseFormattedText(raw: String) = buildAnnotatedString {
    // Basic bold and inline code formatting
    val parts = raw.split("**")
    for (i in parts.indices) {
        val part = parts[i]
        if (i % 2 == 1) {
            // Bold
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(part)
            }
        } else {
            // Check inline code `code`
            val codeParts = part.split("`")
            for (j in codeParts.indices) {
                val cPart = codeParts[j]
                if (j % 2 == 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    ) {
                        append(cPart)
                    }
                } else {
                    append(cPart)
                }
            }
        }
    }
}
