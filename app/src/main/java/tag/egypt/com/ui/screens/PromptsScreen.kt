package tag.egypt.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.R
import tag.egypt.com.model.PromptTemplate
import tag.egypt.com.ui.theme.AmberOrange
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.SettingsViewModel

/**
 * Reusable Prompt Library and Parameter Filler in TAJ EGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onUsePrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompts by viewModel.prompts.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var fillingPrompt by remember { mutableStateOf<PromptTemplate?>(null) }

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
                    text = stringResource(R.string.prompts_title),
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
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.prompts_add),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(prompts, key = { it.id }) { prompt ->
                PromptCard(
                    prompt = prompt,
                    onUse = {
                        val vars = prompt.extractVariables()
                        if (vars.isEmpty()) {
                            onUsePrompt(prompt.userTemplate)
                            onBack()
                        } else {
                            fillingPrompt = prompt
                        }
                    },
                    onDelete = { viewModel.deletePrompt(prompt.id) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreatePromptDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { p ->
                viewModel.savePrompt(p)
                showCreateDialog = false
                Toast.makeText(context, "Prompt template saved", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fillingPrompt?.let { p ->
        FillPromptVariablesDialog(
            prompt = p,
            onDismiss = { fillingPrompt = null },
            onApply = { rendered ->
                fillingPrompt = null
                onUsePrompt(rendered)
                onBack()
            }
        )
    }
}

@Composable
private fun PromptCard(
    prompt: PromptTemplate,
    onUse: () -> Unit,
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
                Text(
                    text = prompt.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prompt.description,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onUse,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.prompts_apply),
                        style = MaterialTheme.typography.labelMedium.copy(color = CyberCyan)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePromptDialog(
    onDismiss: () -> Unit,
    onSave: (PromptTemplate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var template by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prompts_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Prompt Title") },
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
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text("User Template (use {{variable}})") },
                    placeholder = { Text("Analyze the {{language}} code for {{project}}") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && template.isNotEmpty()) {
                        onSave(
                            PromptTemplate(
                                "prompt_" + System.currentTimeMillis(),
                                title.trim(),
                                desc.trim(),
                                "",
                                template.trim(),
                                "custom",
                                false
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
private fun FillPromptVariablesDialog(
    prompt: PromptTemplate,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    val vars = remember(prompt) { prompt.extractVariables() }
    val varValues = remember { mutableStateOf(vars.associateWith { "" }.toMutableMap()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fill Variables for ${prompt.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                vars.forEach { varName ->
                    val currentVal = varValues.value[varName] ?: ""
                    OutlinedTextField(
                        value = currentVal,
                        onValueChange = { newVal ->
                            varValues.value = HashMap(varValues.value).apply { put(varName, newVal) }
                        },
                        label = { Text("{{$varName}}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rendered = prompt.render(varValues.value)
                    onApply(rendered)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Insert into Chat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        containerColor = Slate900
    )
}
