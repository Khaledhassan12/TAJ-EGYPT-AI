package tag.egypt.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.model.McpServer
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.EmeraldGreen
import tag.egypt.com.ui.theme.RoseRed
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.SettingsViewModel

/**
 * Model Context Protocol (MCP) Server and Tool Registry screen in TAJ EGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val servers by viewModel.mcpServers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showImportJsonDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.mcp_title),
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
                IconButton(onClick = { showImportJsonDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = stringResource(R.string.mcp_import_json),
                        tint = CyberCyan
                    )
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.mcp_add),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
        )

        if (servers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.mcp_empty),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.mcp_empty_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(servers, key = { it.id }) { server ->
                    McpServerCard(
                        server = server,
                        onToggle = { updated -> viewModel.saveMcpServer(updated) },
                        onDelete = { viewModel.deleteMcpServer(server.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMcpServerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { server ->
                viewModel.saveMcpServer(server)
                showAddDialog = false
                Toast.makeText(context, "MCP Server configured", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showImportJsonDialog) {
        ImportMcpJsonDialog(
            onDismiss = { showImportJsonDialog = false },
            onImport = { json ->
                viewModel.importMcpJson(
                    json,
                    onSuccess = { count ->
                        showImportJsonDialog = false
                        Toast.makeText(context, "Imported $count MCP servers", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "Import error: $err", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun McpServerCard(
    server: McpServer,
    onToggle: (McpServer) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (server.isEnabled) EmeraldGreen else Slate400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = server.isEnabled,
                        onCheckedChange = { onToggle(server.copyWithEnabled(it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricIndigo)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = "URL: ${server.serverUrl}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Transport: ${server.transport.description}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (server.transport.isSupportedOnAndroid()) CyberCyan else RoseRed,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun AddMcpServerDialog(
    onDismiss: () -> Unit,
    onSave: (McpServer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mcp_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL (SSE / HTTP)") },
                    placeholder = { Text("https://mcp.weather.service/sse") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && url.isNotEmpty()) {
                        onSave(
                            McpServer(
                                "mcp_" + System.currentTimeMillis(),
                                name.trim(),
                                url.trim(),
                                McpServer.Transport.SSE,
                                desc.trim(),
                                true,
                                false,
                                "",
                                "{}",
                                0
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        containerColor = Slate900
    )
}

@Composable
private fun ImportMcpJsonDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mcp_import_json)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Paste standard MCP JSON configuration with mcpServers key.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                )
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    placeholder = { Text("{\n  \"mcpServers\": {\n    \"my-server\": {\n      \"url\": \"https://...\"\n    }\n  }\n}") },
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (jsonText.isNotEmpty()) onImport(jsonText) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        containerColor = Slate900
    )
}
