package tag.egypt.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.ui.components.StreamingAnimationMode
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.EmeraldGreen
import tag.egypt.com.ui.theme.RoseRed
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.ChatViewModel
import tag.egypt.com.ui.viewmodel.SettingsViewModel

/**
 * Settings and Application Preferences screen in TAJ EGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    chatViewModel: ChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by settingsViewModel.currentLanguage.collectAsState()
    val streamEnabled by settingsViewModel.streamEnabled.collectAsState()
    val syntaxHighlight by settingsViewModel.syntaxHighlighting.collectAsState()
    val showLineNumbers by settingsViewModel.showLineNumbers.collectAsState()
    val reduceMotion by settingsViewModel.reduceMotion.collectAsState()
    val lowMemoryMode by settingsViewModel.lowMemoryMode.collectAsState()
    val animMode by settingsViewModel.animationMode.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Settings Group
            item {
                SectionHeader(stringResource(R.string.settings_general))
            }

            item {
                SettingsCard {
                    SettingsSwitchRow(
                        title = "Reduce Motion",
                        subtitle = "Disables fast-pulse and typing cursor effects",
                        checked = reduceMotion,
                        onCheckedChange = { settingsViewModel.reduceMotion.value = it }
                    )

                    HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.settings_animation_mode),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "Current: ${animMode.name}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val nextMode = when (animMode) {
                                    StreamingAnimationMode.TYPING -> StreamingAnimationMode.FADE
                                    StreamingAnimationMode.FADE -> StreamingAnimationMode.SCALE_IN
                                    StreamingAnimationMode.SCALE_IN -> StreamingAnimationMode.NONE
                                    else -> StreamingAnimationMode.TYPING
                                }
                                settingsViewModel.animationMode.value = nextMode
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(animMode.name, style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan))
                        }
                    }
                }
            }

            // Chat & Rendering Settings Group
            item {
                SectionHeader(stringResource(R.string.settings_chat))
            }

            item {
                SettingsCard {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_stream_responses),
                        subtitle = "Render tokens in real-time as they arrive",
                        checked = streamEnabled,
                        onCheckedChange = { settingsViewModel.streamEnabled.value = it }
                    )

                    HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_syntax_highlight),
                        subtitle = "Colorize code blocks in Python, Kotlin, SQL, etc.",
                        checked = syntaxHighlight,
                        onCheckedChange = { settingsViewModel.syntaxHighlighting.value = it }
                    )

                    HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_line_numbers),
                        subtitle = "Show line numbers on code blocks",
                        checked = showLineNumbers,
                        onCheckedChange = { settingsViewModel.showLineNumbers.value = it }
                    )
                }
            }

            // Security & Credentials
            item {
                SectionHeader(stringResource(R.string.settings_security))
            }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_keystore_active),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            settingsViewModel.clearAllCredentials()
                            Toast.makeText(context, R.string.settings_clear_credentials, Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.settings_clear_credentials),
                            style = MaterialTheme.typography.labelMedium.copy(color = RoseRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showClearDataDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.settings_clear_data),
                            style = MaterialTheme.typography.labelMedium.copy(color = RoseRed)
                        )
                    }
                }
            }

            // Performance
            item {
                SectionHeader(stringResource(R.string.settings_performance))
            }

            item {
                SettingsCard {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_low_memory_mode),
                        subtitle = "Minimizes background image caching and animation overhead",
                        checked = lowMemoryMode,
                        onCheckedChange = { settingsViewModel.lowMemoryMode.value = it }
                    )
                }
            }

            // About TAJ EGY
            item {
                SectionHeader(stringResource(R.string.settings_about))
            }

            item {
                SettingsCard {
                    Text(
                        text = "TAJ EGY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigoLight
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Universal AI Client for Android. Local-first, provider-agnostic, and secure.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                }
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Conversations?") },
            text = { Text("This will permanently remove all chat history from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.clearAllConversations()
                        showClearDataDialog = false
                        Toast.makeText(context, "All conversations cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseRed)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = ElectricIndigo)
        )
    }
}
