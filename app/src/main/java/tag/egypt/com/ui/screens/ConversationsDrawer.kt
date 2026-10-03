package tag.egypt.com.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.model.Conversation
import tag.egypt.com.storage.AppDatabase
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate700
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.ChatViewModel

/**
 * Slide-out navigation drawer displaying conversation sessions, search, and system navigation links in TAJ EGY.
 */
@Composable
fun ConversationsDrawer(
    chatViewModel: ChatViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToProviders: () -> Unit,
    onNavigateToMcp: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToPrompts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val conversationsFlow = remember { db.conversationDao().getActiveConversationsFlow() }
    val convEntities by conversationsFlow.collectAsState(initial = emptyList())
    val conversations = remember(convEntities) { convEntities.map { it.toDomain() } }

    val activeConv by chatViewModel.currentConversation.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            conversations
        } else {
            conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Slate950),
        color = Slate950
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            // Header with Brand and New Chat button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(
                    onClick = {
                        chatViewModel.newConversation()
                        onNavigateToChat()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Conversation",
                        tint = CyberCyan
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.chat_search_hint),
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedBorderColor = ElectricIndigoLight,
                    unfocusedBorderColor = Slate800
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conv ->
                    val isSelected = activeConv?.id == conv.id
                    ConversationDrawerItem(
                        conversation = conv,
                        isSelected = isSelected,
                        onClick = {
                            chatViewModel.selectConversation(conv)
                            onNavigateToChat()
                        },
                        onExport = {
                            val json = "{\"id\":\"${conv.id}\",\"title\":\"${conv.title}\",\"created\":${conv.createdAt}}"
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("export", json))
                            Toast.makeText(context, "Conversation JSON copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

            // System Navigation Shortcuts
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                DrawerNavItem(
                    icon = Icons.Default.Cloud,
                    label = stringResource(R.string.nav_providers),
                    onClick = onNavigateToProviders
                )
                DrawerNavItem(
                    icon = Icons.Default.Build,
                    label = stringResource(R.string.nav_mcp),
                    onClick = onNavigateToMcp
                )
                DrawerNavItem(
                    icon = Icons.Default.AutoAwesome,
                    label = stringResource(R.string.nav_skills),
                    onClick = onNavigateToSkills
                )
                DrawerNavItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    label = stringResource(R.string.nav_prompts),
                    onClick = onNavigateToPrompts
                )
                DrawerNavItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.nav_settings),
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun ConversationDrawerItem(
    conversation: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onExport: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) ElectricIndigo.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (conversation.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelected) ElectricIndigoLight else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    maxLines = 1
                )
            }

            IconButton(onClick = onExport, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export conversation",
                    tint = Slate400,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
