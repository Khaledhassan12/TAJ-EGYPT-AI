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
 * Adapter for Alibaba Cloud DashScope / Qwen models in TAJ EGY.
 */
public final class QwenAdapter implements AIProviderAdapter {
    private static final String TAG = "TajQwen";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final List<ModelInfo> presetModels;

    public QwenAdapter() {
        List<ModelInfo> models = new ArrayList<>();
        Set<ModelCapability> caps = EnumSet.of(
                ModelCapability.TEXT_GENERATION,
                ModelCapability.STREAMING,
                ModelCapability.TOOL_CALLING
        );

        models.add(new ModelInfo("qwen-max", "Qwen 2.5 Max (Flagship)", ProviderType.QWEN, 32000, 8192, caps, 2.80, 8.40));
        models.add(new ModelInfo("qwen-plus", "Qwen 2.5 Plus (Balanced)", ProviderType.QWEN, 128000, 8192, caps, 0.40, 1.20));
        models.add(new ModelInfo("qwen-turbo", "Qwen 2.5 Turbo (High Speed)", ProviderType.QWEN, 128000, 8192, caps, 0.10, 0.30));
        models.add(new ModelInfo("qwen-coder-plus", "Qwen 2.5 Coder Plus (Coding)", ProviderType.QWEN, 128000, 8192, caps, 0.50, 1.50));
        this.presetModels = Collections.unmodifiableList(models);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.QWEN;
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
            String endpoint = config.getBaseUrl().endsWith("/models")
                    ? config.getBaseUrl()
                    : config.getBaseUrl() + "/models";

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                long latency = SystemClock.elapsedRealtime() - start;
                if (response.isSuccessful()) {
                    return new ConnectionTestResult(true, response.code(), latency, "Authenticated successfully with Qwen.");
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
                    : "qwen-plus";

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
                    .post(RequestBody.create(root.toString(), JSON_MEDIA_TYPE))
                    .build();

            OkHttpClient client = NetworkClient.getInstance();
            Call call = client.newCall(request);
            cancellationToken.attachCall(call);

            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    String err = "HTTP " + response.code() + " from Qwen";
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
                        Log.d(TAG, "Qwen chunk parse notice: " + e.getMessage());
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
