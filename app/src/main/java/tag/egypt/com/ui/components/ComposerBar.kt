package tag.egypt.com.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.RoseRed
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900

/**
 * Expressive rounded Composer Bar for message authoring in TAJ EGY.
 */
@Composable
fun ComposerBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isGenerating: Boolean,
    onAttachClick: () -> Unit,
    onPromptsClick: () -> Unit,
    onToolsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Slate800,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach button
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = stringResource(R.string.chat_attach),
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Prompt template button
                IconButton(
                    onClick = onPromptsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = stringResource(R.string.chat_prompts),
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_placeholder),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate400.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            ),
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(ElectricIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 24.dp, max = 120.dp)
                    )
                }

                // Tools button
                IconButton(
                    onClick = onToolsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = stringResource(R.string.chat_tools),
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Send or Stop Generation Button
                AnimatedContent(
                    targetState = isGenerating,
                    label = "send_stop_button"
                ) { generating ->
                    if (generating) {
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoseRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = stringResource(R.string.chat_stop),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        val canSend = text.trim().isNotEmpty()
                        IconButton(
                            onClick = { if (canSend) onSend() },
                            enabled = canSend,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (canSend) ElectricIndigo else Slate400.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.chat_send),
                                tint = if (canSend) Color.White else Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
