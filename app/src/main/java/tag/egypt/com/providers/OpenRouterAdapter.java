package tag.egypt.com.providers;

import android.os.SystemClock;
import android.util.Log;

import tag.egypt.com.model.ChatMessage;
import tag.egypt.com.model.ChatStreamEvent;
import tag.egypt.com.model.ModelCapability;
import tag.egypt.com.model.ModelInfo;
import tag.egypt.com.model.ProviderConfig;
import tag.egypt.com.model.ProviderType;
import tag.egypt.com.network.CancellationToken;
import tag.egypt.com.network.NetworkClient;
import tag.egypt.com.network.SseStreamReader;
import tag.egypt.com.network.StreamCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Adapter for OpenRouter API in TAJ EGY.
 */
public final class OpenRouterAdapter implements AIProviderAdapter {
    private static final String TAG = "TajOpenRouter";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final List<ModelInfo> presetModels;

    public OpenRouterAdapter() {
        List<ModelInfo> models = new ArrayList<>();
        Set<ModelCapability> standardCaps = EnumSet.of(
                ModelCapability.TEXT_GENERATION,
                ModelCapability.STREAMING,
                ModelCapability.TOOL_CALLING
        );

        models.add(new ModelInfo("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B Instruct", ProviderType.OPENROUTER, 128000, 8192, standardCaps, 0.40, 0.40));
        models.add(new ModelInfo("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet (via OpenRouter)", ProviderType.OPENROUTER, 200000, 8192, standardCaps, 3.00, 15.00));
        models.add(new ModelInfo("google/gemini-2.0-flash-exp:free", "Gemini 2.0 Flash (Free Tier)", ProviderType.OPENROUTER, 1000000, 8192, standardCaps, 0.0, 0.0));
        models.add(new ModelInfo("mistralai/mistral-large-2411", "Mistral Large 2411", ProviderType.OPENROUTER, 128000, 8192, standardCaps, 2.00, 6.00));
        this.presetModels = Collections.unmodifiableList(models);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.OPENROUTER;
    }

    @Override
    public List<ModelInfo> getAvailableModels() {
        return presetModels;
    }

    @Override
    public Set<ModelCapability> getCapabilities(String modelId) {
        for (ModelInfo info : presetModels) {
            if (info.getModelId().equalsIgnoreCase(modelId)) {
                return info.getCapabilities();
            }
        }
        return EnumSet.of(ModelCapability.TEXT_GENERATION, ModelCapability.STREAMING);
    }

    @Override
    public ConnectionTestResult testConnection(ProviderConfig config) {
        long start = SystemClock.elapsedRealtime();
        try {
            OkHttpClient client = NetworkClient.getProbeClient();
            String endpoint = config.getBaseUrl() + "/auth/key";

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("HTTP-Referer", "https://tajegy.app")
                    .addHeader("X-Title", "TAJ EGY")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                long latency = SystemClock.elapsedRealtime() - start;
                if (response.isSuccessful()) {
                    return new ConnectionTestResult(true, response.code(), latency, "Authenticated successfully with OpenRouter.");
                } else {
                    return new ConnectionTestResult(false, response.code(), latency, "HTTP " + response.code() + ": " + response.message());
                }
            }
        } catch (Exception e) {
            long latency = SystemClock.elapsedRealtime() - start;
            return new ConnectionTestResult(false, 0, latency, "Connection error: " + e.getMessage());
        }
    }

    @Override
    public void generateStream(
            ProviderConfig config,
            List<ChatMessage> history,
            String systemPrompt,
            StreamCallback callback,
            CancellationToken cancellationToken
    ) {
        try {
            JSONObject root = new JSONObject();
            String model = (config.getDefaultModelId() != null && !config.getDefaultModelId().isEmpty())
                    ? config.getDefaultModelId()
                    : "meta-llama/llama-3.3-70b-instruct";

            root.put("model", model);
            root.put("stream", true);
            root.put("temperature", config.getTemperature());

            JSONArray messagesArray = new JSONArray();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                JSONObject sys = new JSONObject();
                sys.put("role", "system");
                sys.put("content", systemPrompt.trim());
                messagesArray.put(sys);
            }

            for (ChatMessage msg : history) {
                JSONObject m = new JSONObject();
                m.put("role", msg.getRole().name().toLowerCase());
                m.put("content", msg.getContent());
                messagesArray.put(m);
            }
            root.put("messages", messagesArray);

            String endpoint = config.getBaseUrl();
            if (!endpoint.endsWith("/chat/completions")) {
                endpoint = endpoint.endsWith("/") ? endpoint + "chat/completions" : endpoint + "/chat/completions";
            }

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("HTTP-Referer", "https://tajegy.app")
                    .addHeader("X-Title", "TAJ EGY")
                    .post(RequestBody.create(root.toString(), JSON_MEDIA_TYPE))
                    .build();

            OkHttpClient client = NetworkClient.getInstance();
            Call call = client.newCall(request);
            cancellationToken.attachCall(call);

            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    String err = "HTTP " + response.code() + " from OpenRouter";
                    if (response.body() != null) {
                        try {
                            JSONObject errJson = new JSONObject(response.body().string());
                            if (errJson.has("error")) {
                                err = errJson.getJSONObject("error").optString("message", err);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    callback.onEvent(ChatStreamEvent.error(err));
                    return;
                }

                SseStreamReader.read(response, cancellationToken, (eventType, data) -> {
                    try {
                        JSONObject chunk = new JSONObject(data);
                        if (chunk.has("choices")) {
                            JSONArray choices = chunk.getJSONArray("choices");
                            if (choices.length() > 0) {
                                JSONObject choice = choices.getJSONObject(0);
                                if (choice.has("delta") && choice.getJSONObject("delta").has("content")) {
                                    String text = choice.getJSONObject("delta").getString("content");
                                    callback.onEvent(ChatStreamEvent.token(text));
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "OpenRouter chunk parse notice: " + e.getMessage());
                    }
                });

                callback.onEvent(ChatStreamEvent.completed(0, 0));
            }
        } catch (IOException e) {
            if (cancellationToken.isCancelled()) return;
            callback.onEvent(ChatStreamEvent.error("Network error: " + e.getMessage()));
        } catch (Exception e) {
            callback.onEvent(ChatStreamEvent.error("Error: " + e.getMessage()));
        }
    }
}
