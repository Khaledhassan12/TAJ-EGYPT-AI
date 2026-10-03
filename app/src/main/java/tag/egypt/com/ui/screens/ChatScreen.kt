package tag.egypt.com.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.model.Attachment
import tag.egypt.com.model.ChatMessage
import tag.egypt.com.ui.components.AiActivityCard
import tag.egypt.com.ui.components.AttachmentsPreviewRow
import tag.egypt.com.ui.components.ComposerBar
import tag.egypt.com.ui.components.MarkdownRenderer
import tag.egypt.com.ui.components.ModelSelectorSheet
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.ChatViewModel
import tag.egypt.com.ui.viewmodel.SettingsViewModel

/**
 * Primary Real-Time AI Chat screen for TAJ EGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenMcp: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenPrompts: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentConv by chatViewModel.currentConversation.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val isGenerating by chatViewModel.isGenerating.collectAsState()
    val streamingMsg by chatViewModel.streamingMessage.collectAsState()
    val providers by chatViewModel.providers.collectAsState()
    val selectedProvider by chatViewModel.selectedProvider.collectAsState()
    val selectedModelId by chatViewModel.selectedModelId.collectAsState()
    val pendingAttachments by chatViewModel.pendingAttachments.collectAsState()

    val animationMode by settingsViewModel.animationMode.collectAsState()
    val showLineNumbers by settingsViewModel.showLineNumbers.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showModelSelector by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive or when streaming
    LaunchedEffect(messages.size, streamingMsg?.content?.length) {
        val totalCount = messages.size + (if (streamingMsg != null) 1 else 0)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Navigation Bar
        TopAppBar(
            title = {
                // Model & Provider Chip Selector
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showModelSelector = true },
                    color = Slate900,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedModelId} • ${selectedProvider?.name ?: "Provider"}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Switch model",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open drawer",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(onClick = { chatViewModel.newConversation() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.chat_new_conversation),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Slate950,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Chat Messages Timeline
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (messages.isEmpty() && streamingMsg == null) {
                // Polished Empty State
                ChatEmptyState(
                    onStarterClick = { prompt ->
                        inputText = prompt
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            showLineNumbers = showLineNumbers,
                            onCopy = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("chat", msg.content))
                                Toast.makeText(context, R.string.code_copied, Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { chatViewModel.deleteMessage(msg.id) },
                            onRegenerate = {
                                chatViewModel.sendMessage("Please clarify or expand on the previous answer.")
                            },
                            onRetryWithAnotherModel = { showModelSelector = true }
                        )
                    }

                    // Active streaming message placeholder
                    streamingMsg?.let { sMsg ->
                        item(key = "active_stream") {
                            MessageBubble(
                                message = sMsg,
                                showLineNumbers = showLineNumbers,
                                isStreaming = true,
                                animationMode = animationMode,
                                onCopy = {},
                                onDelete = {},
                                onRegenerate = {},
                                onRetryWithAnotherModel = {}
                            )
                        }
                    }
                }
            }
        }

        // Attachments Preview
        AttachmentsPreviewRow(
            attachments = pendingAttachments,
            onRemove = { chatViewModel.removeAttachment(it) }
        )

        // Bottom Composer Bar
        ComposerBar(
            text = inputText,
            onTextChanged = { inputText = it },
            onSend = {
                chatViewModel.sendMessage(inputText)
                inputText = ""
            },
            onStop = { chatViewModel.stopGeneration() },
            isGenerating = isGenerating,
            onAttachClick = {
                // Quick sample text attachment for testing capability
                val sampleAtt = Attachment(
                    java.util.UUID.randomUUID().toString(),
                    "project_notes.txt",
                    "text/plain",
                    1024,
                    "",
                    ""
                )
                chatViewModel.addAttachment(sampleAtt)
                Toast.makeText(context, "Attachment added to message", Toast.LENGTH_SHORT).show()
            },
            onPromptsClick = onOpenPrompts,
            onToolsClick = onOpenMcp
        )
    }

    // Model Selector Bottom Sheet
    if (showModelSelector) {
        ModelSelectorSheet(
            providers = providers,
            currentProvider = selectedProvider,
            currentModelId = selectedModelId,
            onSelect = { prov, modelId ->
                chatViewModel.selectProviderAndModel(prov, modelId)
            },
            onDismiss = { showModelSelector = false }
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    showLineNumbers: Boolean,
    isStreaming: Boolean = false,
    animationMode: tag.egypt.com.ui.components.StreamingAnimationMode = tag.egypt.com.ui.components.StreamingAnimationMode.TYPING,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRegenerate: () -> Unit,
    onRetryWithAnotherModel: () -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Message Bubble
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                color = ElectricIndigo,
                tonalElevation = 2.dp,
                modifier = Modifier.padding(start = 48.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        } else {
            // Assistant Message Surface
            Row(modifier = Modifier.fillMaxWidth().padding(end = 12.dp)) {
                // AI Avatar Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(ElectricIndigo, CyberCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Message Content Rendered as Markdown & Code
                    MarkdownRenderer(
                        content = message.content,
                        isStreaming = isStreaming,
                        showLineNumbers = showLineNumbers,
                        animationMode = animationMode
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // AI Activity Card (Telemetry & Latency)
                    if (!isStreaming) {
                        AiActivityCard(message = message)
                    }

                    // Message Action Buttons
                    if (!isStreaming && message.content.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy response",
                                    tint = Slate400,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(onClick = onRegenerate, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = Slate400,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(onClick = onRetryWithAnotherModel, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Retry with another model",
                                    tint = Slate400,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Slate400,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyState(
    onStarterClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricIndigoLight.copy(alpha = 0.35f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.chat_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.chat_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = Slate400),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Starter Prompts
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StarterCard(
                title = "Design a Jetpack Compose UI",
                subtitle = "Create modern M3 layout tokens and animations",
                onClick = { onStarterClick("Show me a clean Jetpack Compose Material 3 card layout with smooth elevation animations.") }
            )
            StarterCard(
                title = "Write a Python Web Scraper",
                subtitle = "Using asyncio, httpx, and BeautifulSoup",
                onClick = { onStarterClick("Write an asynchronous Python web scraper using httpx and asyncio with proper rate limiting.") }
            )
            StarterCard(
                title = "Explain Quantum Computing",
                subtitle = "In clear, intuitive terms with practical analogies",
                onClick = { onStarterClick("Explain the core principles of quantum computing and qubits using simple, intuitive analogies.") }
            )
        }
    }
}

@Composable
private fun StarterCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
        }
    }
}
