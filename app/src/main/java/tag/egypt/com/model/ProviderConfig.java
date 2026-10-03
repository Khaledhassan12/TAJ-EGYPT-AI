package tag.egypt.com.model;

import java.util.Objects;

/**
 * Complete runtime configuration for an AI provider instance in TAJ EGY.
 *
 * <p>What this component does:
 * Holds credentials, endpoints, operational parameters (temperature, top-p, timeouts),
 * and model configurations for a specific provider setup.
 *
 * <p>Security considerations:
 * API keys are never stored in plain text in logs or public models. When displayed in UI,
 * {@link #getMaskedApiKey()} must be used instead of the raw key.
 */
public final class ProviderConfig {
    private final String id;
    private final ProviderType providerType;
    private final String name;
    private final String apiKey;
    private final String baseUrl;
    private final String defaultModelId;
    private final boolean isEnabled;
    private final boolean isDefault;
    private final float temperature;
    private final float topP;
    private final int maxTokens;
    private final boolean streamEnabled;
    private final String systemPrompt;
    private final int timeoutSeconds;
    private final String organizationId;

    public ProviderConfig(
            String id,
            ProviderType providerType,
            String name,
            String apiKey,
            String baseUrl,
            String defaultModelId,
            boolean isEnabled,
            boolean isDefault,
            float temperature,
            float topP,
            int maxTokens,
            boolean streamEnabled,
            String systemPrompt,
            int timeoutSeconds,
            String organizationId
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.providerType = Objects.requireNonNull(providerType, "providerType cannot be null");
        this.name = (name != null && !name.trim().isEmpty()) ? name : providerType.getDisplayName();
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.baseUrl = (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl.trim() : providerType.getDefaultBaseUrl();
        this.defaultModelId = defaultModelId != null ? defaultModelId : "";
        this.isEnabled = isEnabled;
        this.isDefault = isDefault;
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.streamEnabled = streamEnabled;
        this.systemPrompt = systemPrompt != null ? systemPrompt : "";
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 60;
        this.organizationId = organizationId != null ? organizationId : "";
    }

    public String getId() {
        return id;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public String getName() {
        return name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getDefaultModelId() {
        return defaultModelId;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getTopP() {
        return topP;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public boolean isStreamEnabled() {
        return streamEnabled;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            return "No key configured";
        }
        if (apiKey.length() <= 4) {
            return "••••";
        }
        String suffix = apiKey.substring(apiKey.length() - 4);
        return "••••••••••••" + suffix;
    }

    public boolean hasValidCredential() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
