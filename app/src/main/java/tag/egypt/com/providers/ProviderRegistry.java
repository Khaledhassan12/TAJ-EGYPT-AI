package tag.egypt.com.providers;

import tag.egypt.com.model.ModelCapability;
import tag.egypt.com.model.ModelInfo;
import tag.egypt.com.model.ProviderType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central registry mapping ProviderTypes to their concrete AIProviderAdapter implementations in TAJ EGY.
 */
public final class ProviderRegistry {
    private static volatile ProviderRegistry instance;
    private final Map<ProviderType, AIProviderAdapter> adapters = new EnumMap<>(ProviderType.class);

    private ProviderRegistry() {
        registerAdapter(new OpenAIAdapter());
        registerAdapter(new AnthropicAdapter());
        registerAdapter(new GeminiAdapter());
        registerAdapter(new DeepSeekAdapter());
        registerAdapter(new OpenRouterAdapter());
        registerAdapter(new QwenAdapter());
        registerAdapter(new GenericOpenAIAdapter(ProviderType.GENERIC_OPENAI));
        registerAdapter(new GenericOpenAIAdapter(ProviderType.CUSTOM));
    }

    public static ProviderRegistry getInstance() {
        if (instance == null) {
            synchronized (ProviderRegistry.class) {
                if (instance == null) {
                    instance = new ProviderRegistry();
                }
            }
        }
        return instance;
    }

    public void registerAdapter(AIProviderAdapter adapter) {
        if (adapter != null) {
            adapters.put(adapter.getProviderType(), adapter);
        }
    }

    public AIProviderAdapter getAdapter(ProviderType type) {
        AIProviderAdapter adapter = adapters.get(type);
        if (adapter == null) {
            return adapters.get(ProviderType.GENERIC_OPENAI);
        }
        return adapter;
    }

    public List<ModelInfo> getModelsForProvider(ProviderType type) {
        AIProviderAdapter adapter = getAdapter(type);
        return adapter != null ? adapter.getAvailableModels() : Collections.emptyList();
    }

    public Set<ModelCapability> getCapabilities(ProviderType type, String modelId) {
        AIProviderAdapter adapter = getAdapter(type);
        return adapter != null ? adapter.getCapabilities(modelId) : Collections.emptySet();
    }
}
