package tag.egypt.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.Slate800

@Composable
fun MarkdownRenderer(
    content: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showLineNumbers: Boolean = true,
    animationMode: StreamingAnimationMode = StreamingAnimationMode.TYPING
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            val isLastBlock = (index == blocks.size - 1)
            when (block.type) {
                BlockType.CODE -> {
                    CodeBlockView(
                        code = block.content,
                        language = block.extra,
                        showLineNumbers = showLineNumbers,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                BlockType.MATH -> {
                    DisplayMathCard(
                        mathExpression = block.content,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                BlockType.HEADING_1 -> {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                BlockType.HEADING_2 -> {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 3.dp)
                    )
                }
                BlockType.HEADING_3 -> {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                BlockType.BLOCKQUOTE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(24.dp)
                                .background(ElectricIndigoLight)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = block.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
                BlockType.LIST_ITEM -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = ElectricIndigoLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = renderInlineFormatting(block.content, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
                BlockType.PARAGRAPH -> {
                    if (isLastBlock && isStreaming) {
                        StreamingTextRenderer(
                            text = block.content,
                            isStreaming = true,
                            color = textColor,
                            animationMode = animationMode,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = renderInlineFormatting(block.content, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

enum class BlockType {
    PARAGRAPH,
    CODE,
    MATH,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    BLOCKQUOTE,
    LIST_ITEM
}

data class MarkdownBlock(
    val type: BlockType,
    val content: String,
    val extra: String = ""
)

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    if (text.isEmpty()) return emptyList()

    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var inCodeBlock = false
    var codeLang = ""
    val codeAccumulator = StringBuilder()
    val paragraphAccumulator = StringBuilder()

    fun flushParagraph() {
        if (paragraphAccumulator.isNotEmpty()) {
            blocks.add(MarkdownBlock(BlockType.PARAGRAPH, paragraphAccumulator.toString().trim()))
            paragraphAccumulator.setLength(0)
        }
    }

    for (line in lines) {
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock(BlockType.CODE, codeAccumulator.toString(), codeLang))
                codeAccumulator.setLength(0)
                codeLang = ""
                inCodeBlock = false
            } else {
                flushParagraph()
                inCodeBlock = true
                codeLang = trimmed.substring(3).trim()
            }
            continue
        }

        if (inCodeBlock) {
            if (codeAccumulator.isNotEmpty()) codeAccumulator.append("\n")
            codeAccumulator.append(line)
            continue
        }

        if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 4) {
            flushParagraph()
            blocks.add(MarkdownBlock(BlockType.MATH, trimmed))
            continue
        }

        if (trimmed.startsWith("# ")) {
            flushParagraph()
            blocks.add(MarkdownBlock(BlockType.HEADING_1, trimmed.substring(2)))
        } else if (trimmed.startsWith("## ")) {
            flushParagraph()
            blocks.add(MarkdownBlock(BlockType.HEADING_2, trimmed.substring(3)))
        } else if (trimmed.startsWith("### ")) {
            flushParagraph()
            blocks.add(MarkdownBlock(BlockType.HEADING_3, trimmed.substring(4)))
        } else if (trimmed.startsWith("> ")) {
            flushParagraph()
            blocks.add(MarkdownBlock(BlockType.BLOCKQUOTE, trimmed.substring(2)))
        } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            flushParagraph()
            blocks.add(MarkdownBlock(BlockType.LIST_ITEM, trimmed.substring(2)))
        } else if (trimmed.isEmpty()) {
            flushParagraph()
        } else {
            if (paragraphAccumulator.isNotEmpty()) paragraphAccumulator.append("\n")
            paragraphAccumulator.append(line)
        }
    }

    if (inCodeBlock) {
        blocks.add(MarkdownBlock(BlockType.CODE, codeAccumulator.toString(), codeLang))
    } else {
        flushParagraph()
    }

    return blocks
}

fun renderInlineFormatting(raw: String, defaultColor: Color): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < raw.length) {
            if (i + 1 < raw.length && raw[i] == '*' && raw[i + 1] == '*') {
                val end = raw.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor))
                    append(raw.substring(i + 2, end))
                    pop()
                    i = end + 2
                    continue
                }
            }
            if (raw[i] == '`') {
                val end = raw.indexOf('`', i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Slate800, color = ElectricIndigoLight))
                    append(" ${raw.substring(i + 1, end)} ")
                    pop()
                    i = end + 1
                    continue
                }
            }
            append(raw[i])
            i++
        }
    }
}
