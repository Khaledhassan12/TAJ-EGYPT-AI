package tag.egypt.com.model;

/**
 * Event emitted during real-time response generation and streaming in TAJ EGY.
 */
public final class ChatStreamEvent {

    public enum Type {
        TOKEN,
        REASONING_UPDATE,
        TOOL_CALL_DETECTED,
        COMPLETED,
        ERROR
    }

    private final Type type;
    private final String textChunk;
    private final String reasoningChunk;
    private final String toolName;
    private final String toolArgsJson;
    private final int inputTokens;
    private final int outputTokens;
    private final String errorMessage;

    private ChatStreamEvent(
            Type type,
            String textChunk,
            String reasoningChunk,
            String toolName,
            String toolArgsJson,
            int inputTokens,
            int outputTokens,
            String errorMessage
    ) {
        this.type = type;
        this.textChunk = textChunk != null ? textChunk : "";
        this.reasoningChunk = reasoningChunk != null ? reasoningChunk : "";
        this.toolName = toolName != null ? toolName : "";
        this.toolArgsJson = toolArgsJson != null ? toolArgsJson : "";
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }

    public static ChatStreamEvent token(String chunk) {
        return new ChatStreamEvent(Type.TOKEN, chunk, "", "", "", 0, 0, "");
    }

    public static ChatStreamEvent reasoning(String summary) {
        return new ChatStreamEvent(Type.REASONING_UPDATE, "", summary, "", "", 0, 0, "");
    }

    public static ChatStreamEvent toolCall(String name, String argsJson) {
        return new ChatStreamEvent(Type.TOOL_CALL_DETECTED, "", "", name, argsJson, 0, 0, "");
    }

    public static ChatStreamEvent completed(int inputTokens, int outputTokens) {
        return new ChatStreamEvent(Type.COMPLETED, "", "", "", "", inputTokens, outputTokens, "");
    }

    public static ChatStreamEvent error(String message) {
        return new ChatStreamEvent(Type.ERROR, "", "", "", "", 0, 0, message);
    }

    public Type getType() {
        return type;
    }

    public String getTextChunk() {
        return textChunk;
    }

    public String getReasoningChunk() {
        return reasoningChunk;
    }

    public String getToolName() {
        return toolName;
    }

    public String getToolArgsJson() {
        return toolArgsJson;
    }

    public int getInputTokens() {
        return inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
