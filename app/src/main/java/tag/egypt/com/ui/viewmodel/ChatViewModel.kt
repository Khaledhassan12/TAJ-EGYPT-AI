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
import tag.egypt.com.engine.ChatEngine
import tag.egypt.com.model.Attachment
import tag.egypt.com.model.ChatMessage
import tag.egypt.com.model.ChatStreamEvent
import tag.egypt.com.model.Conversation
import tag.egypt.com.model.ProviderConfig
import tag.egypt.com.network.CancellationToken
import tag.egypt.com.providers.ProviderRegistry
import tag.egypt.com.security.SecureCredentialStore
import tag.egypt.com.storage.AppDatabase
import tag.egypt.com.storage.ConversationEntity
import tag.egypt.com.storage.MessageEntity
import tag.egypt.com.storage.OmniDatabaseManager

/**
 * Primary ViewModel orchestrating real-time chat sessions in TAJ EGY.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val chatEngine = ChatEngine.getInstance(application)
    private val credentialStore = SecureCredentialStore.getInstance(application)

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingMessage = MutableStateFlow<ChatMessage?>(null)
    val streamingMessage: StateFlow<ChatMessage?> = _streamingMessage.asStateFlow()

    private val _providers = MutableStateFlow<List<ProviderConfig>>(emptyList())
    val providers: StateFlow<List<ProviderConfig>> = _providers.asStateFlow()

    private val _selectedProvider = MutableStateFlow<ProviderConfig?>(null)
    val selectedProvider: StateFlow<ProviderConfig?> = _selectedProvider.asStateFlow()

    private val _selectedModelId = MutableStateFlow("gpt-4o")
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    private val _pendingAttachments = MutableStateFlow<List<Attachment>>(emptyList())
    val pendingAttachments: StateFlow<List<Attachment>> = _pendingAttachments.asStateFlow()

    private var activeCancellationToken: CancellationToken? = null
    private var messagesJob: kotlinx.coroutines.Job? = null

    init {
        // Initialize pre-seeded database presets if needed
        OmniDatabaseManager.initializeDefaultsAsync(application)

        // Observe providers
        viewModelScope.launch(Dispatchers.IO) {
            db.providerDao().getAllProvidersFlow().collectLatest { entities ->
                val list = entities.map { it.toDomain(credentialStore.getApiKey(it.id)) }
                _providers.value = list

                if (_selectedProvider.value == null && list.isNotEmpty()) {
                    val defaultProv = list.firstOrNull { it.isDefault } ?: list.first()
                    _selectedProvider.value = defaultProv
                    _selectedModelId.value = defaultProv.defaultModelId.ifEmpty { "gpt-4o" }
                }
            }
        }

        // Start initial conversation session
        newConversation()
    }

    fun newConversation() {
        val newConv = Conversation(
            java.util.UUID.randomUUID().toString(),
            "New Chat",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            _selectedProvider.value?.id ?: "provider_openai",
            _selectedModelId.value,
            "",
            false,
            false,
            0
        )
        _currentConversation.value = newConv
        _messages.value = emptyList()
        _streamingMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            db.conversationDao().insertOrUpdate(ConversationEntity.fromDomain(newConv))
            observeMessagesForConversation(newConv.id)
        }
    }

    fun selectConversation(conv: Conversation) {
        _currentConversation.value = conv
        _streamingMessage.value = null
        observeMessagesForConversation(conv.id)
    }

    private fun observeMessagesForConversation(convId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch(Dispatchers.IO) {
            db.messageDao().getMessagesForConversationFlow(convId).collectLatest { entities ->
                _messages.value = entities.map { it.toDomain() }
            }
        }
    }

    fun selectProviderAndModel(provider: ProviderConfig, modelId: String) {
        _selectedProvider.value = provider
        _selectedModelId.value = modelId
        _currentConversation.value?.let { current ->
            val updated = current.copyWithModel(provider.id, modelId)
            _currentConversation.value = updated
            viewModelScope.launch(Dispatchers.IO) {
                db.conversationDao().insertOrUpdate(ConversationEntity.fromDomain(updated))
            }
        }
    }

    fun addAttachment(attachment: Attachment) {
        _pendingAttachments.value = _pendingAttachments.value + attachment
    }

    fun removeAttachment(attachment: Attachment) {
        _pendingAttachments.value = _pendingAttachments.value.filter { it.id != attachment.id }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() && _pendingAttachments.value.isEmpty()) return
        val conv = _currentConversation.value ?: return
        val provider = _selectedProvider.value ?: return

        activeCancellationToken?.cancel()
        _isGenerating.value = true
        val attachments = _pendingAttachments.value
        _pendingAttachments.value = emptyList()

        val cancelToken = CancellationToken()
        activeCancellationToken = cancelToken

        // Setup streaming preview placeholder
        val placeholder = ChatMessage(
            java.util.UUID.randomUUID().toString(),
            conv.id,
            ChatMessage.Role.ASSISTANT,
            "",
            System.currentTimeMillis(),
            _selectedModelId.value,
            provider.name,
            0,
            0,
            0,
            0,
            0.0,
            ChatMessage.Status.STREAMING,
            "",
            emptyList(),
            "",
            ""
        )
        _streamingMessage.value = placeholder

        val contentBuffer = StringBuilder()

        chatEngine.sendMessage(
            conv,
            trimmed,
            attachments,
            provider,
            _selectedModelId.value,
            { event ->
                when (event.type) {
                    ChatStreamEvent.Type.TOKEN -> {
                        contentBuffer.append(event.textChunk)
                        _streamingMessage.value = placeholder.copyWithStreamingContent(contentBuffer.toString())
                    }
                    ChatStreamEvent.Type.REASONING_UPDATE -> {
                        // Safe reasoning status
                    }
                    ChatStreamEvent.Type.COMPLETED,
                    ChatStreamEvent.Type.ERROR -> {
                        _isGenerating.value = false
                        _streamingMessage.value = null
                    }
                    else -> {}
                }
            },
            cancelToken
        )
    }

    fun retryLastMessage() {
        val currentMsgs = _messages.value
        if (currentMsgs.isEmpty()) return
        val lastUserMsg = currentMsgs.lastOrNull { it.role == ChatMessage.Role.USER }
        if (lastUserMsg != null) {
            val lastAssistantMsg = currentMsgs.lastOrNull()
            if (lastAssistantMsg != null && lastAssistantMsg.role == ChatMessage.Role.ASSISTANT) {
                deleteMessage(lastAssistantMsg.id)
            }
            sendMessage(lastUserMsg.content)
        }
    }

    fun regenerateMessage(message: ChatMessage) {
        if (message.role == ChatMessage.Role.ASSISTANT) {
            deleteMessage(message.id)
            retryLastMessage()
        }
    }

    fun stopGeneration() {
        activeCancellationToken?.cancel()
        _isGenerating.value = false
        _streamingMessage.value = null
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.messageDao().deleteById(messageId)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            db.conversationDao().deleteAll()
            newConversation()
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
        activeCancellationToken?.cancel()
    }
}
