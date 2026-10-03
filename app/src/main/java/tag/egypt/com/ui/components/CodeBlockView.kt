package tag.egypt.com.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.EmeraldGreen
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate700
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900

/**
 * Professional code block renderer with syntax highlighting in TAJ EGY.
 */
@Composable
fun CodeBlockView(
    code: String,
    language: String,
    modifier: Modifier = Modifier,
    showLineNumbers: Boolean = true
) {
    val context = LocalContext.current
    var isWrapped by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }
    var isCopied by remember { mutableStateOf(false) }

    val cleanLang = language.ifBlank { detectLanguage(code) }
    val lines = remember(code) { code.lines() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Slate900,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate700)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cleanLang.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isWrapped = !isWrapped },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.WrapText,
                            contentDescription = "Toggle word wrap",
                            tint = if (isWrapped) CyberCyan else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                            isCopied = true
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) EmeraldGreen else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand or collapse",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                val scrollState = rememberScrollState()
                val contentModifier = if (isWrapped) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.horizontalScroll(scrollState)
                }

                Row(
                    modifier = contentModifier
                        .padding(12.dp)
                        .heightIn(max = 500.dp)
                ) {
                    if (showLineNumbers) {
                        Column(modifier = Modifier.padding(end = 12.dp)) {
                            lines.indices.forEach { idx ->
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate400.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }

                    Column {
                        lines.forEach { line ->
                            Text(
                                text = highlightLine(line, cleanLang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun highlightLine(line: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        val trimmed = line.trim()
        if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("--")) {
            pushStyle(SpanStyle(color = Color(0xFF6B7280), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
            append(line)
            pop()
            return@buildAnnotatedString
        }

        val keywords = setOf(
            "val", "var", "fun", "class", "interface", "import", "package", "return", "if", "else",
            "while", "for", "when", "def", "async", "await", "const", "let", "function", "public",
            "private", "protected", "static", "void", "select", "from", "where", "insert", "update"
        )

        var i = 0
        while (i < line.length) {
            when {
                line[i] == '"' || line[i] == '\'' -> {
                    val quote = line[i]
                    val nextQuote = line.indexOf(quote, i + 1)
                    if (nextQuote != -1) {
                        pushStyle(SpanStyle(color = EmeraldGreen))
                        append(line.substring(i, nextQuote + 1))
                        pop()
                        i = nextQuote + 1
                    } else {
                        pushStyle(SpanStyle(color = EmeraldGreen))
                        append(line.substring(i))
                        pop()
                        break
                    }
                }
                line[i].isLetter() -> {
                    val start = i
                    while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) {
                        i++
                    }
                    val word = line.substring(start, i)
                    if (keywords.contains(word.lowercase())) {
                        pushStyle(SpanStyle(color = ElectricIndigoLight, fontWeight = FontWeight.Bold))
                        append(word)
                        pop()
                    } else if (word.first().isUpperCase()) {
                        pushStyle(SpanStyle(color = CyberCyan))
                        append(word)
                        pop()
                    } else {
                        append(word)
                    }
                }
                line[i].isDigit() -> {
                    val start = i
                    while (i < line.length && (line[i].isDigit() || line[i] == '.')) {
                        i++
                    }
                    pushStyle(SpanStyle(color = Color(0xFFF59E0B)))
                    append(line.substring(start, i))
                    pop()
                }
                else -> {
                    append(line[i])
                    i++
                }
            }
        }
    }
}

private fun detectLanguage(code: String): String {
    val lower = code.lowercase()
    return when {
        lower.contains("fun ") || lower.contains("val ") || lower.contains("package ") -> "kotlin"
        lower.contains("def ") || lower.contains("import numpy") || lower.contains("print(") -> "python"
        lower.contains("public class") || lower.contains("system.out.println") -> "java"
        lower.contains("const ") || lower.contains("console.log") -> "javascript"
        lower.contains("select ") && lower.contains("from ") -> "sql"
        lower.contains("<html>") || lower.contains("<div>") -> "html"
        lower.contains("curl ") || lower.contains("#!/bin/bash") -> "bash"
        lower.startsWith("{") || lower.startsWith("[") -> "json"
        else -> "code"
    }
}
