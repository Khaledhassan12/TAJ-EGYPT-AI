package tag.egypt.com.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tag.egypt.com.mcp.McpProtocolParser
import tag.egypt.com.model.McpServer
import tag.egypt.com.model.PromptTemplate
import tag.egypt.com.model.ProviderConfig
import tag.egypt.com.model.Skill
import tag.egypt.com.network.BaseUrlDiscoveryService
import tag.egypt.com.providers.ProviderRegistry
import tag.egypt.com.security.SecureCredentialStore
import tag.egypt.com.skills.SkillManager
import tag.egypt.com.storage.AppDatabase
import tag.egypt.com.storage.McpServerEntity
import tag.egypt.com.storage.PromptEntity
import tag.egypt.com.storage.ProviderEntity
import tag.egypt.com.storage.SkillEntity
import tag.egypt.com.ui.components.StreamingAnimationMode
import java.io.InputStream

/**
 * ViewModel managing application configuration, providers, MCP, and skills in TAJ EGY.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val credentialStore = SecureCredentialStore.getInstance(application)

    // Settings States
    val isDarkMode = MutableStateFlow(true)
    val currentLanguage = MutableStateFlow("en-US") // "en-US" or "ar-EG"
    val streamEnabled = MutableStateFlow(true)
    val syntaxHighlighting = MutableStateFlow(true)
    val showLineNumbers = MutableStateFlow(true)
    val reduceMotion = MutableStateFlow(false)
    val lowMemoryMode = MutableStateFlow(false)
    val animationMode = MutableStateFlow(StreamingAnimationMode.TYPING)

    // Entities Flows
    private val _providers = MutableStateFlow<List<ProviderConfig>>(emptyList())
    val providers: StateFlow<List<ProviderConfig>> = _providers.asStateFlow()

    private val _mcpServers = MutableStateFlow<List<McpServer>>(emptyList())
    val mcpServers: StateFlow<List<McpServer>> = _mcpServers.asStateFlow()

    private val _skills = MutableStateFlow<List<Skill>>(emptyList())
    val skills: StateFlow<List<Skill>> = _skills.asStateFlow()

    private val _prompts = MutableStateFlow<List<PromptTemplate>>(emptyList())
    val prompts: StateFlow<List<PromptTemplate>> = _prompts.asStateFlow()

    init {
        // Collect providers
        viewModelScope.launch(Dispatchers.IO) {
            db.providerDao().getAllProvidersFlow().collectLatest { entities ->
                _providers.value = entities.map { it.toDomain(credentialStore.getApiKey(it.id)) }
            }
        }

        // Collect MCP servers
        viewModelScope.launch(Dispatchers.IO) {
            db.mcpDao().getAllServersFlow().collectLatest { entities ->
                _mcpServers.value = entities.map { it.toDomain() }
            }
        }

        // Collect skills
        viewModelScope.launch(Dispatchers.IO) {
            db.skillDao().getAllSkillsFlow().collectLatest { entities ->
                _skills.value = entities.map { it.toDomain() }
            }
        }

        // Collect prompts
        viewModelScope.launch(Dispatchers.IO) {
            db.promptDao().getAllPromptsFlow().collectLatest { entities ->
                _prompts.value = entities.map { it.toDomain() }
            }
        }
    }

    // Provider Management
    fun saveProvider(config: ProviderConfig, rawApiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (rawApiKey.isNotEmpty()) {
                credentialStore.saveApiKey(config.id, rawApiKey)
            }
            db.providerDao().insertOrUpdate(ProviderEntity.fromDomain(config))
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            credentialStore.deleteApiKey(providerId)
            db.providerDao().deleteById(providerId)
        }
    }

    fun testProviderConnection(config: ProviderConfig, onResult: (Boolean, Long, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val key = credentialStore.getApiKey(config.id).ifEmpty { config.apiKey }
            val runtimeConfig = ProviderConfig(
                config.id, config.providerType, config.name, key, config.baseUrl,
                config.defaultModelId, config.isEnabled, config.isDefault,
                config.temperature, config.topP, config.maxTokens,
                config.isStreamEnabled, config.systemPrompt, config.timeoutSeconds,
                config.organizationId
            )
            val adapter = ProviderRegistry.getInstance().getAdapter(config.providerType)
            val res = adapter.testConnection(runtimeConfig)
            onResult(res.isSuccess, res.latencyMs, res.message)
        }
    }

    fun autoDetectProvider(url: String, apiKey: String, onResult: (BaseUrlDiscoveryService.DiscoveryResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = BaseUrlDiscoveryService.discover(url, apiKey)
            onResult(res)
        }
    }

    // MCP Management
    fun saveMcpServer(server: McpServer) {
        viewModelScope.launch(Dispatchers.IO) {
            db.mcpDao().insertOrUpdate(McpServerEntity.fromDomain(server))
        }
    }

    fun deleteMcpServer(serverId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.mcpDao().deleteById(serverId)
        }
    }

    fun importMcpJson(json: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        try {
            val servers = McpProtocolParser.parseMcpConfigJson(json)
            viewModelScope.launch(Dispatchers.IO) {
                servers.forEach { s ->
                    db.mcpDao().insertOrUpdate(McpServerEntity.fromDomain(s))
                }
                onSuccess(servers.size)
            }
        } catch (e: Exception) {
            onError(e.message ?: "Failed parsing JSON")
        }
    }

    // Skill Management
    fun saveSkill(skill: Skill) {
        viewModelScope.launch(Dispatchers.IO) {
            db.skillDao().insertOrUpdate(SkillEntity.fromDomain(skill))
        }
    }

    fun deleteSkill(skillId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.skillDao().deleteById(skillId)
        }
    }

    fun toggleSkillEnabled(skill: Skill) {
        viewModelScope.launch(Dispatchers.IO) {
            db.skillDao().setEnabled(skill.id, !skill.isEnabled)
        }
    }

    fun importSkillZip(inputStream: InputStream, onSuccess: (Skill) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val skill = SkillManager.importSkillZip(getApplication(), inputStream)
                db.skillDao().insertOrUpdate(SkillEntity.fromDomain(skill))
                onSuccess(skill)
            } catch (e: Exception) {
                onError(e.message ?: "ZIP import error")
            }
        }
    }

    // Prompt Management
    fun savePrompt(prompt: PromptTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            db.promptDao().insertOrUpdate(PromptEntity.fromDomain(prompt))
        }
    }

    fun deletePrompt(promptId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.promptDao().deleteById(promptId)
        }
    }

    fun clearAllCredentials() {
        credentialStore.clearAllCredentials()
    }
}
