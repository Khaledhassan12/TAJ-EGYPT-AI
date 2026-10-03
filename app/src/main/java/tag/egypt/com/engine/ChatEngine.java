package tag.egypt.com.engine;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import tag.egypt.com.model.Attachment;
import tag.egypt.com.model.ChatMessage;
import tag.egypt.com.model.ChatStreamEvent;
import tag.egypt.com.model.Conversation;
import tag.egypt.com.model.ModelInfo;
import tag.egypt.com.model.ProviderConfig;
import tag.egypt.com.model.Skill;
import tag.egypt.com.network.CancellationToken;
import tag.egypt.com.network.StreamCallback;
import tag.egypt.com.providers.AIProviderAdapter;
import tag.egypt.com.providers.ProviderRegistry;
import tag.egypt.com.security.SecureCredentialStore;
import tag.egypt.com.skills.SkillMatcher;
import tag.egypt.com.storage.AppDatabase;
import tag.egypt.com.storage.ConversationEntity;
import tag.egypt.com.storage.MessageEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Orchestrator for sending messages, executing AI inference, and persisting state in TAJ EGY.
 */
public final class ChatEngine {
    private static final String TAG = "TajChatEngine";
    private static volatile ChatEngine instance;
    private final Context appContext;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private ChatEngine(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static ChatEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (ChatEngine.class) {
                if (instance == null) {
                    instance = new ChatEngine(context);
                }
            }
        }
        return instance;
    }

    public void sendMessage(
            Conversation conversation,
            String userPrompt,
            List<Attachment> attachments,
            ProviderConfig config,
            String selectedModelId,
            StreamCallback streamCallback,
            CancellationToken cancellationToken
    ) {
        executorService.execute(() -> {
            long startTime = SystemClock.elapsedRealtime();
            AppDatabase db = AppDatabase.Companion.getInstance(appContext);

            try {
                ChatMessage userMsg = new ChatMessage(
                        java.util.UUID.randomUUID().toString(),
                        conversation.getId(),
                        ChatMessage.Role.USER,
                        userPrompt,
                        System.currentTimeMillis(),
                        selectedModelId,
                        config.getName(),
                        0,
                        estimateTokens(userPrompt),
                        0,
                        estimateTokens(userPrompt),
                        0.0,
                        ChatMessage.Status.COMPLETED,
                        "",
                        attachments,
                        "",
                        ""
                );
                db.messageDao().insertOrUpdate(MessageEntity.Companion.fromDomain(userMsg));

                List<MessageEntity> entities = db.messageDao().getMessagesForConversation(conversation.getId());
                List<ChatMessage> history = new ArrayList<>();
                for (MessageEntity me : entities) {
                    history.add(me.toDomain());
                }

                StringBuilder effectiveSystemPrompt = new StringBuilder();
                if (conversation.getSystemPrompt() != null && !conversation.getSystemPrompt().trim().isEmpty()) {
                    effectiveSystemPrompt.append(conversation.getSystemPrompt().trim()).append("\n\n");
                } else if (config.getSystemPrompt() != null && !config.getSystemPrompt().trim().isEmpty()) {
                    effectiveSystemPrompt.append(config.getSystemPrompt().trim()).append("\n\n");
                }

                List<Skill> enabledSkills = new ArrayList<>();
                for (var se : db.skillDao().getEnabledSkills()) {
                    enabledSkills.add(se.toDomain());
                }
                Skill matchedSkill = SkillMatcher.findBestMatchingSkill(userPrompt, enabledSkills);
                if (matchedSkill != null) {
                    effectiveSystemPrompt.append("=== APPLIED SKILL: ").append(matchedSkill.getName()).append(" ===\n")
                            .append(matchedSkill.getInstructions()).append("\n\n");
                    Log.i(TAG, "Applied matched skill: " + matchedSkill.getName());
                }

                String rawApiKey = SecureCredentialStore.getInstance(appContext).getApiKey(config.getId());
                if (rawApiKey.isEmpty() && config.hasValidCredential()) {
                    rawApiKey = config.getApiKey();
                }

                ProviderConfig runtimeConfig = new ProviderConfig(
                        config.getId(),
                        config.getProviderType(),
                        config.getName(),
                        rawApiKey,
                        config.getBaseUrl(),
                        selectedModelId,
                        config.isEnabled(),
                        config.isDefault(),
                        config.getTemperature(),
                        config.getTopP(),
                        config.getMaxTokens(),
                        config.isStreamEnabled(),
                        config.getSystemPrompt(),
                        config.getTimeoutSeconds(),
                        config.getOrganizationId()
                );

                AIProviderAdapter adapter = ProviderRegistry.getInstance().getAdapter(runtimeConfig.getProviderType());
                String assistantMsgId = java.util.UUID.randomUUID().toString();
                StringBuilder responseBuffer = new StringBuilder();

                adapter.generateStream(
                        runtimeConfig,
                        history,
                        effectiveSystemPrompt.toString(),
                        new StreamCallback() {
                            @Override
                            public void onEvent(ChatStreamEvent event) {
                                if (event.getType() == ChatStreamEvent.Type.TOKEN) {
                                    responseBuffer.append(event.getTextChunk());
                                }
                                streamCallback.onEvent(event);
                            }
                        },
                        cancellationToken
                );

                long latency = SystemClock.elapsedRealtime() - startTime;
                int inTokens = estimateTokens(userPrompt);
                int outTokens = estimateTokens(responseBuffer.toString());
                double estimatedCost = calculateCost(selectedModelId, adapter.getAvailableModels(), inTokens, outTokens);

                ChatMessage assistantMsg = new ChatMessage(
                        assistantMsgId,
                        conversation.getId(),
                        ChatMessage.Role.ASSISTANT,
                        responseBuffer.toString(),
                        System.currentTimeMillis(),
                        selectedModelId,
                        config.getName(),
                        latency,
                        inTokens,
                        outTokens,
                        inTokens + outTokens,
                        estimatedCost,
                        cancellationToken.isCancelled() ? ChatMessage.Status.IDLE : ChatMessage.Status.COMPLETED,
                        "",
                        Collections.emptyList(),
                        "",
                        matchedSkill != null ? "Skill: " + matchedSkill.getName() : ""
                );
                db.messageDao().insertOrUpdate(MessageEntity.Companion.fromDomain(assistantMsg));

                int totalMsgs = db.messageDao().getMessageCount(conversation.getId());
                Conversation updatedConv = new Conversation(
                        conversation.getId(),
                        (totalMsgs <= 2 && userPrompt.length() > 0)
                                ? summarizeTitle(userPrompt)
                                : conversation.getTitle(),
                        conversation.getCreatedAt(),
                        System.currentTimeMillis(),
                        config.getId(),
                        selectedModelId,
                        conversation.getSystemPrompt(),
                        conversation.isPinned(),
                        conversation.isArchived(),
                        totalMsgs
                );
                db.conversationDao().insertOrUpdate(ConversationEntity.Companion.fromDomain(updatedConv));

            } catch (Exception e) {
                Log.e(TAG, "Error executing chat stream: " + e.getMessage(), e);
                streamCallback.onEvent(ChatStreamEvent.error(e.getMessage()));
            }
        });
    }

    private static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return Math.max(1, (int) Math.ceil(text.length() / 3.8));
    }

    private static double calculateCost(String modelId, List<ModelInfo> models, int inTokens, int outTokens) {
        for (ModelInfo info : models) {
            if (info.getModelId().equalsIgnoreCase(modelId)) {
                double inCost = (inTokens / 1_000_000.0) * info.getInputCostPer1M();
                double outCost = (outTokens / 1_000_000.0) * info.getOutputCostPer1M();
                return inCost + outCost;
            }
        }
        return 0.0;
    }

    private static String summarizeTitle(String prompt) {
        if (prompt.length() <= 36) return prompt;
        return prompt.substring(0, 33).trim() + "…";
    }
}
