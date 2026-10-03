package tag.egypt.com.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fundamental data model representing a message in a conversation in TAJ EGY.
 */
public final class ChatMessage {

    public enum Role {
        USER,
        ASSISTANT,
        SYSTEM,
        TOOL
    }

    public enum Status {
        IDLE,
        STREAMING,
        COMPLETED,
        ERROR
    }

    private final String id;
    private final String conversationId;
    private final Role role;
    private final String content;
    private final long timestamp;
    private final String modelUsed;
    private final String providerUsed;
    private final long latencyMs;
    private final int inputTokens;
    private final int outputTokens;
    private final int totalTokens;
    private final double estimatedCost;
    private final Status status;
    private final String errorMessage;
    private final List<Attachment> attachments;
    private final String toolCallsJson;
    private final String reasoningSummary;

    public ChatMessage(
            String id,
            String conversationId,
            Role role,
            String content,
            long timestamp,
            String modelUsed,
            String providerUsed,
            long latencyMs,
            int inputTokens,
            int outputTokens,
            int totalTokens,
            double estimatedCost,
            Status status,
            String errorMessage,
            List<Attachment> attachments,
            String toolCallsJson,
            String reasoningSummary
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.role = role != null ? role : Role.USER;
        this.content = content != null ? content : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.modelUsed = modelUsed != null ? modelUsed : "";
        this.providerUsed = providerUsed != null ? providerUsed : "";
        this.latencyMs = latencyMs;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens > 0 ? totalTokens : (inputTokens + outputTokens);
        this.estimatedCost = estimatedCost;
        this.status = status != null ? status : Status.COMPLETED;
        this.errorMessage = errorMessage != null ? errorMessage : "";
        this.attachments = attachments != null
                ? Collections.unmodifiableList(new ArrayList<>(attachments))
                : Collections.emptyList();
        this.toolCallsJson = toolCallsJson != null ? toolCallsJson : "";
        this.reasoningSummary = reasoningSummary != null ? reasoningSummary : "";
    }

    public String getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public String getProviderUsed() {
        return providerUsed;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public int getInputTokens() {
        return inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getToolCallsJson() {
        return toolCallsJson;
    }

    public String getReasoningSummary() {
        return reasoningSummary;
    }

    public boolean isUser() {
        return role == Role.USER;
    }

    public boolean isAssistant() {
        return role == Role.ASSISTANT;
    }

    public boolean isStreaming() {
        return status == Status.STREAMING;
    }

    public boolean hasError() {
        return status == Status.ERROR;
    }

    public ChatMessage copyWithStreamingContent(String newContent) {
        return new ChatMessage(
                this.id,
                this.conversationId,
                this.role,
                newContent,
                this.timestamp,
                this.modelUsed,
                this.providerUsed,
                this.latencyMs,
                this.inputTokens,
                this.outputTokens,
                this.totalTokens,
                this.estimatedCost,
                Status.STREAMING,
                this.errorMessage,
                this.attachments,
                this.toolCallsJson,
                this.reasoningSummary
        );
    }
}
