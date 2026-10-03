package tag.egypt.com.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Metadata and specifications for an AI model.
 *
 * <p>What this component does:
 * Holds model identifiers, display names, context window sizes, supported capabilities,
 * and cost estimation metrics per 1M tokens.
 */
public final class ModelInfo {
    private final String modelId;
    private final String displayName;
    private final ProviderType providerType;
    private final int contextWindow;
    private final int maxOutputTokens;
    private final Set<ModelCapability> capabilities;
    private final double inputCostPer1M;
    private final double outputCostPer1M;

    public ModelInfo(
            String modelId,
            String displayName,
            ProviderType providerType,
            int contextWindow,
            int maxOutputTokens,
            Set<ModelCapability> capabilities,
            double inputCostPer1M,
            double outputCostPer1M
    ) {
        this.modelId = Objects.requireNonNull(modelId, "modelId cannot be null");
        this.displayName = displayName != null ? displayName : modelId;
        this.providerType = Objects.requireNonNull(providerType, "providerType cannot be null");
        this.contextWindow = contextWindow;
        this.maxOutputTokens = maxOutputTokens;
        this.capabilities = capabilities != null
                ? Collections.unmodifiableSet(EnumSet.copyOf(capabilities))
                : Collections.emptySet();
        this.inputCostPer1M = inputCostPer1M;
        this.outputCostPer1M = outputCostPer1M;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public int getContextWindow() {
        return contextWindow;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public Set<ModelCapability> getCapabilities() {
        return capabilities;
    }

    public boolean supports(ModelCapability capability) {
        return capabilities.contains(capability);
    }

    public double getInputCostPer1M() {
        return inputCostPer1M;
    }

    public double getOutputCostPer1M() {
        return outputCostPer1M;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelInfo modelInfo = (ModelInfo) o;
        return Objects.equals(modelId, modelInfo.modelId) && providerType == modelInfo.providerType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, providerType);
    }

    @Override
    public String toString() {
        return displayName + " (" + modelId + ")";
    }
}
