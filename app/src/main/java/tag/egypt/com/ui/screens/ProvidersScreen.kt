package tag.egypt.com.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.model.ProviderConfig
import tag.egypt.com.model.ProviderType
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.EmeraldGreen
import tag.egypt.com.ui.theme.RoseRed
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate700
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.SettingsViewModel

/**
 * AI Provider & API Credential Management screen in TAJ EGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val providers by viewModel.providers.collectAsState()

    var editingProvider by remember { mutableStateOf<ProviderConfig?>(null) }
    var testResultMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var testingId by remember { mutableStateOf<String?>(null) }
    var showAutoDetectDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.nav_providers),
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
            actions = {
                IconButton(onClick = { showAutoDetectDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = stringResource(R.string.provider_detect),
                        tint = CyberCyan
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(providers, key = { it.id }) { provider ->
                ProviderCard(
                    provider = provider,
                    testResult = testResultMap[provider.id],
                    isTesting = testingId == provider.id,
                    onEdit = { editingProvider = provider },
                    onTest = {
                        testingId = provider.id
                        viewModel.testProviderConnection(provider) { success, latency, msg ->
                            testingId = null
                            val status = if (success) "✓ Reachable (${latency}ms)" else "✗ Failed: $msg"
                            testResultMap = testResultMap + (provider.id to status)
                        }
                    }
                )
            }
        }
    }

    // Edit Provider Modal Dialog
    editingProvider?.let { config ->
        EditProviderDialog(
            config = config,
            onDismiss = { editingProvider = null },
            onSave = { updated, key ->
                viewModel.saveProvider(updated, key)
                editingProvider = null
                Toast.makeText(context, R.string.provider_saved, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Auto-Detect Provider Modal Dialog
    if (showAutoDetectDialog) {
        AutoDetectDialog(
            onDismiss = { showAutoDetectDialog = false },
            onDetect = { url, key ->
                viewModel.autoDetectProvider(url, key) { result ->
                    val newProv = ProviderConfig(
                        "prov_custom_" + System.currentTimeMillis(),
                        result.detectedType,
                        result.detectedType.displayName,
                        key,
                        result.normalizedUrl,
                        result.recommendedModelId,
                        true,
                        false,
                        0.7f,
                        1.0f,
                        4096,
                        true,
                        "",
                        60,
                        ""
                    )
                    viewModel.saveProvider(newProv, key)
                    showAutoDetectDialog = false
                    Toast.makeText(context, "Detected: ${result.detectedType.displayName}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
private fun ProviderCard(
    provider: ProviderConfig,
    testResult: String?,
    isTesting: Boolean,
    onEdit: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (provider.hasValidCredential()) EmeraldGreen else Slate400)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Provider",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Endpoint: ${provider.baseUrl}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Key: ${provider.maskedApiKey}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )

            testResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (result.startsWith("✓")) EmeraldGreen else RoseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onTest,
                    enabled = !isTesting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTesting) "Pinging…" else "Test Connection",
                        style = MaterialTheme.typography.labelMedium.copy(color = CyberCyan)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProviderDialog(
    config: ProviderConfig,
    onDismiss: () -> Unit,
    onSave: (ProviderConfig, String) -> Unit
) {
    var baseUrl by remember { mutableStateOf(config.baseUrl) }
    var rawApiKey by remember { mutableStateOf("") }
    var defaultModel by remember { mutableStateOf(config.defaultModelId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Configure ${config.name}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawApiKey,
                    onValueChange = { rawApiKey = it },
                    label = { Text("New API Key (Leave blank to keep current)") },
                    placeholder = { Text(config.maskedApiKey) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = defaultModel,
                    onValueChange = { defaultModel = it },
                    label = { Text("Default Model ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = ProviderConfig(
                        config.id,
                        config.providerType,
                        config.name,
                        config.apiKey,
                        baseUrl,
                        defaultModel,
                        config.isEnabled,
                        config.isDefault,
                        config.temperature,
                        config.topP,
                        config.maxTokens,
                        config.isStreamEnabled,
                        config.systemPrompt,
                        config.timeoutSeconds,
                        config.organizationId
                    )
                    onSave(updated, rawApiKey)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = Slate900
    )
}

@Composable
private fun AutoDetectDialog(
    onDismiss: () -> Unit,
    onDetect: (String, String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.provider_detect))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.provider_detect_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("API Endpoint URL") },
                    placeholder = { Text("https://api.openai.com/v1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Optional for probe)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.trim().isNotEmpty()) {
                        onDetect(url.trim(), apiKey.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Detect & Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = Slate900
    )
}
