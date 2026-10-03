package tag.egypt.com.model;

/**
 * Declares fine-grained capabilities supported by specific AI models and providers.
 *
 * <p>What this component does:
 * Flags distinct capabilities such as streaming, vision, tool calling, structured JSON output,
 * and reasoning metadata.
 *
 * <p>Why it exists:
 * The application enforces capability-aware UI. If a model does not support
 * vision or tool calling, the UI disables those affordances.
 */
public enum ModelCapability {
    TEXT_GENERATION("Text Generation"),
    STREAMING("Streaming Responses"),
    VISION("Image & Vision Input"),
    FILE_INPUT("File Attachments"),
    JSON_OUTPUT("Structured JSON Output"),
    TOOL_CALLING("Function & Tool Calling"),
    PARALLEL_TOOL_CALLS("Parallel Tool Calls"),
    MCP_COMPATIBLE("MCP Server Integration"),
    REASONING_STATUS("Reasoning Status Display"),
    SYSTEM_INSTRUCTIONS("System Instructions"),
    AUDIO_INPUT("Audio Input");

    private final String label;

    ModelCapability(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
