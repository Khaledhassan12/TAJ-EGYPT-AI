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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import tag.egypt.com.model.Skill
import tag.egypt.com.skills.SkillSuggester
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.ElectricIndigo
import tag.egypt.com.ui.theme.ElectricIndigoLight
import tag.egypt.com.ui.theme.EmeraldGreen
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.viewmodel.SettingsViewModel

/**
 * AI Skills Management and Selection screen in TAJ EGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val skills by viewModel.skills.collectAsState()

    var showManualAddDialog by remember { mutableStateOf(false) }

    val zipPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    viewModel.importSkillZip(
                        inputStream = inputStream,
                        onSuccess = { skill ->
                            Toast.makeText(context, "Skill '${skill.name}' imported successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Import failed: $error", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val recommendations = remember(skills) {
        SkillSuggester.checkRecommendations("docker sql query indexing", skills)
    }

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
                    text = stringResource(R.string.skills_title),
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
                IconButton(onClick = { zipPickerLauncher.launch("application/zip") }) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = stringResource(R.string.skills_import_zip),
                        tint = ElectricIndigoLight
                    )
                }
                IconButton(onClick = { showManualAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.skills_add_manual),
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
            item {
                Text(
                    text = "INSTALLED SKILLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(skills, key = { it.id }) { skill ->
                SkillCard(
                    skill = skill,
                    onToggle = { viewModel.toggleSkillEnabled(skill) },
                    onDelete = { viewModel.deleteSkill(skill.id) }
                )
            }

            if (recommendations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.skills_recommended),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            letterSpacing = 1.sp
                        )
                    )
                }

                items(recommendations) { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rec.reason,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.saveSkill(rec.recommendedSkill)
                                    Toast.makeText(context, "Skill installed successfully", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Install Skill", style = MaterialTheme.typography.labelMedium.copy(color = CyberCyan))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showManualAddDialog) {
        ManualSkillDialog(
            onDismiss = { showManualAddDialog = false },
            onSave = { skill ->
                viewModel.saveSkill(skill)
                showManualAddDialog = false
                Toast.makeText(context, "Skill saved", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun SkillCard(
    skill: Skill,
    onToggle: () -> Unit,
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
                            .background(if (skill.isEnabled) EmeraldGreen else Slate400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = skill.isEnabled,
                        onCheckedChange = { onToggle() },
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

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
            if (skill.triggerHints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Triggers: ${skill.triggerHints}",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberCyan, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
private fun ManualSkillDialog(
    onDismiss: () -> Unit,
    onSave: (Skill) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.skills_add_manual)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Skill Name") },
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
                    value = triggers,
                    onValueChange = { triggers = it },
                    label = { Text("Trigger Keywords (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("System Instructions") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && instructions.isNotEmpty()) {
                        onSave(
                            Skill(
                                "skill_" + System.currentTimeMillis(),
                                name.trim(),
                                "1.0.0",
                                desc.trim(),
                                instructions.trim(),
                                triggers.trim(),
                                triggers.trim(),
                                5,
                                true,
                                Skill.Source.MANUAL,
                                "[]"
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
