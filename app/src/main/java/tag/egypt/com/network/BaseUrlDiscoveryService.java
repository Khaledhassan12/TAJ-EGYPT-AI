package tag.egypt.com.network;

import android.util.Log;

import tag.egypt.com.model.ModelCapability;
import tag.egypt.com.model.ProviderType;
import tag.egypt.com.security.SecurityValidator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service providing safe, non-invasive Auto-Detection for AI Provider endpoints in TAJ EGY.
 */
public final class BaseUrlDiscoveryService {
    private static final String TAG = "TajDiscovery";

    public static final class DiscoveryResult {
        private final boolean success;
        private final ProviderType detectedType;
        private final String normalizedUrl;
        private final String recommendedModelId;
        private final List<String> discoveredModels;
        private final Set<ModelCapability> capabilities;
        private final String notes;
        private final boolean isPrivateNetwork;

        public DiscoveryResult(
                boolean success,
                ProviderType detectedType,
                String normalizedUrl,
                String recommendedModelId,
                List<String> discoveredModels,
                Set<ModelCapability> capabilities,
                String notes,
                boolean isPrivateNetwork
        ) {
            this.success = success;
            this.detectedType = detectedType;
            this.normalizedUrl = normalizedUrl;
            this.recommendedModelId = recommendedModelId;
            this.discoveredModels = discoveredModels != null ? discoveredModels : Collections.emptyList();
            this.capabilities = capabilities != null ? capabilities : Collections.emptySet();
            this.notes = notes != null ? notes : "";
            this.isPrivateNetwork = isPrivateNetwork;
        }

        public boolean isSuccess() {
            return success;
        }

        public ProviderType getDetectedType() {
            return detectedType;
        }

        public String getNormalizedUrl() {
            return normalizedUrl;
        }

        public String getRecommendedModelId() {
            return recommendedModelId;
        }

        public List<String> getDiscoveredModels() {
            return discoveredModels;
        }

        public Set<ModelCapability> getCapabilities() {
            return capabilities;
        }

        public String getNotes() {
            return notes;
        }

        public boolean isPrivateNetwork() {
            return isPrivateNetwork;
        }
    }

    private BaseUrlDiscoveryService() {
        // Utility
    }

    public static DiscoveryResult discover(String inputUrl, String optionalApiKey) {
        String normalized;
        try {
            normalized = SecurityValidator.validateAndNormalizeUrl(inputUrl);
        } catch (Exception e) {
            return new DiscoveryResult(
                    false,
                    ProviderType.GENERIC_OPENAI,
                    inputUrl,
                    "",
                    Collections.emptyList(),
                    Collections.emptySet(),
                    "Invalid URL: " + e.getMessage(),
                    false
            );
        }

        boolean isPrivate = SecurityValidator.isPrivateOrLocalHost(normalized);
        String lower = normalized.toLowerCase();
        ProviderType detectedType;
        String recommendedModel;
        Set<ModelCapability> capabilities = EnumSet.of(
                ModelCapability.TEXT_GENERATION,
                ModelCapability.STREAMING
        );

        if (lower.contains("openai.com")) {
            detectedType = ProviderType.OPENAI;
            recommendedModel = "gpt-4o";
            capabilities.add(ModelCapability.VISION);
            capabilities.add(ModelCapability.TOOL_CALLING);
            capabilities.add(ModelCapability.PARALLEL_TOOL_CALLS);
            capabilities.add(ModelCapability.JSON_OUTPUT);
        } else if (lower.contains("anthropic.com")) {
            detectedType = ProviderType.ANTHROPIC;
            recommendedModel = "claude-3-5-sonnet-20241022";
            capabilities.add(ModelCapability.VISION);
            capabilities.add(ModelCapability.TOOL_CALLING);
        } else if (lower.contains("generativelanguage.googleapis.com") || lower.contains("gemini")) {
            detectedType = ProviderType.GEMINI;
            recommendedModel = "gemini-1.5-pro";
            capabilities.add(ModelCapability.VISION);
            capabilities.add(ModelCapability.TOOL_CALLING);
            capabilities.add(ModelCapability.FILE_INPUT);
        } else if (lower.contains("deepseek.com")) {
            detectedType = ProviderType.DEEPSEEK;
            recommendedModel = "deepseek-chat";
            capabilities.add(ModelCapability.REASONING_STATUS);
            capabilities.add(ModelCapability.TOOL_CALLING);
        } else if (lower.contains("openrouter.ai")) {
            detectedType = ProviderType.OPENROUTER;
            recommendedModel = "meta-llama/llama-3.3-70b-instruct";
            capabilities.add(ModelCapability.TOOL_CALLING);
        } else if (lower.contains("aliyuncs.com") || lower.contains("dashscope") || lower.contains("qwen")) {
            detectedType = ProviderType.QWEN;
            recommendedModel = "qwen-plus";
            capabilities.add(ModelCapability.TOOL_CALLING);
        } else {
            detectedType = ProviderType.GENERIC_OPENAI;
            recommendedModel = "default-model";
        }

        List<String> discoveredModels = new ArrayList<>();
        if (optionalApiKey != null && !optionalApiKey.trim().isEmpty() && !isPrivate) {
            probeModels(normalized, detectedType, optionalApiKey, discoveredModels);
        }

        String notes = "Detected " + detectedType.getDisplayName() + " architecture.";
        if (isPrivate) {
            notes += " (Warning: Endpoint is on a private/local network).";
        }

        return new DiscoveryResult(
                true,
                detectedType,
                normalized,
                recommendedModel,
                discoveredModels,
                capabilities,
                notes,
                isPrivate
        );
    }

    private static void probeModels(String baseUrl, ProviderType type, String apiKey, List<String> outModels) {
        try {
            OkHttpClient client = NetworkClient.getProbeClient();
            String probeUrl;
            Request.Builder reqBuilder = new Request.Builder();

            if (type == ProviderType.ANTHROPIC) {
                return;
            } else if (type == ProviderType.GEMINI) {
                probeUrl = baseUrl + "/v1beta/models?key=" + apiKey;
            } else {
                probeUrl = baseUrl.endsWith("/v1") ? baseUrl + "/models" : baseUrl + "/v1/models";
                reqBuilder.addHeader("Authorization", "Bearer " + apiKey);
            }

            reqBuilder.url(probeUrl).get();
            try (Response response = client.newCall(reqBuilder.build()).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    if (obj.has("data")) {
                        JSONArray data = obj.getJSONArray("data");
                        for (int i = 0; i < Math.min(data.length(), 20); i++) {
                            JSONObject m = data.getJSONObject(i);
                            if (m.has("id")) {
                                outModels.add(m.getString("id"));
                            }
                        }
                    } else if (obj.has("models")) {
                        JSONArray models = obj.getJSONArray("models");
                        for (int i = 0; i < Math.min(models.length(), 20); i++) {
                            JSONObject m = models.getJSONObject(i);
                            if (m.has("name")) {
                                String name = m.getString("name");
                                outModels.add(name.replace("models/", ""));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Optional model probe skipped or timed out: " + e.getMessage());
        }
    }
}
