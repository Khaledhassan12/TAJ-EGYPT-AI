package tag.egypt.com.model;

/**
 * Enumeration of all supported AI providers in TAJ EGY.
 *
 * <p>What this component does:
 * Defines the core identity for AI provider services, including major commercial AI vendors,
 * open-router aggregators, and generic standards-compliant endpoints.
 *
 * <p>Why it exists:
 * The app is strictly provider-agnostic. This enum serves as the discriminant key for
 * selecting the appropriate adapter in {@code ProviderRegistry}.
 *
 * <p>How it communicates:
 * Used across the network layer, storage entities, view models, and provider adapters to
 * route requests to the correct communication protocol.
 *
 * <p>Lifecycle &amp; Threading:
 * Immutable enum, safe across all threads and lifecycles.
 */
public enum ProviderType {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1"),
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
    QWEN("Qwen / Alibaba Cloud", "https://dashscope-intl.aliyuncs.com/compatible-mode/v1"),
    GENERIC_OPENAI("OpenAI Compatible", ""),
    CUSTOM("Custom Provider", "");

    private final String displayName;
    private final String defaultBaseUrl;

    ProviderType(String displayName, String defaultBaseUrl) {
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }
}
