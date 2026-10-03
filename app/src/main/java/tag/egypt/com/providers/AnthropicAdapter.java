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
 * Adapter implementing the Anthropic Claude Messages API in TAJ EGY.
 */
public final class AnthropicAdapter implements AIProviderAdapter {
    private static final String TAG = "TajAnthropic";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final List<ModelInfo> presetModels;

    public AnthropicAdapter() {
        List<ModelInfo> models = new ArrayList<>();
        Set<ModelCapability> caps = EnumSet.of(
                ModelCapability.TEXT_GENERATION,
                ModelCapability.STREAMING,
                ModelCapability.VISION,
                ModelCapability.TOOL_CALLING
        );

        models.add(new ModelInfo("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet (State-of-the-Art)", ProviderType.ANTHROPIC, 200000, 8192, caps, 3.00, 15.00));
        models.add(new ModelInfo("claude-3-5-haiku-20241022", "Claude 3.5 Haiku (Lightning Fast)", ProviderType.ANTHROPIC, 200000, 8192, caps, 0.80, 4.00));
        models.add(new ModelInfo("claude-3-opus-20240229", "Claude 3 Opus (Complex Analysis)", ProviderType.ANTHROPIC, 200000, 4096, caps, 15.00, 75.00));
        this.presetModels = Collections.unmodifiableList(models);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.ANTHROPIC;
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
            String endpoint = config.getBaseUrl();
            if (!endpoint.endsWith("/messages")) {
                endpoint = endpoint.endsWith("/") ? endpoint + "messages" : endpoint + "/messages";
            }

            JSONObject root = new JSONObject();
            root.put("model", "claude-3-5-haiku-20241022");
            root.put("max_tokens", 1);
            JSONArray msgs = new JSONArray();
            JSONObject m = new JSONObject();
            m.put("role", "user");
            m.put("content", "hi");
            msgs.put(m);
            root.put("messages", msgs);

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("x-api-key", config.getApiKey())
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .post(RequestBody.create(root.toString(), JSON_MEDIA_TYPE))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                long latency = SystemClock.elapsedRealtime() - start;
                if (response.isSuccessful()) {
                    return new ConnectionTestResult(true, response.code(), latency, "Authenticated successfully with Anthropic.");
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
                    : "claude-3-5-sonnet-20241022";

            root.put("model", model);
            root.put("max_tokens", config.getMaxTokens() > 0 ? config.getMaxTokens() : 4096);
            root.put("stream", true);
            root.put("temperature", config.getTemperature());

            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                root.put("system", systemPrompt.trim());
            }

            JSONArray messagesArray = new JSONArray();
            for (ChatMessage msg : history) {
                if (msg.getRole() == ChatMessage.Role.SYSTEM) continue;
                JSONObject m = new JSONObject();
                m.put("role", msg.getRole() == ChatMessage.Role.ASSISTANT ? "assistant" : "user");
                m.put("content", msg.getContent());
                messagesArray.put(m);
            }
            root.put("messages", messagesArray);

            String endpoint = config.getBaseUrl();
            if (!endpoint.endsWith("/messages")) {
                endpoint = endpoint.endsWith("/") ? endpoint + "messages" : endpoint + "/messages";
            }

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("x-api-key", config.getApiKey())
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .post(RequestBody.create(root.toString(), JSON_MEDIA_TYPE))
                    .build();

            OkHttpClient client = NetworkClient.getInstance();
            Call call = client.newCall(request);
            cancellationToken.attachCall(call);

            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    String err = "HTTP " + response.code() + " from Anthropic";
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
                        JSONObject json = new JSONObject(data);
                        String type = json.optString("type");
                        if ("content_block_delta".equals(type) && json.has("delta")) {
                            JSONObject delta = json.getJSONObject("delta");
                            if ("text_delta".equals(delta.optString("type"))) {
                                String text = delta.optString("text");
                                callback.onEvent(ChatStreamEvent.token(text));
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Anthropic chunk error: " + e.getMessage());
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
