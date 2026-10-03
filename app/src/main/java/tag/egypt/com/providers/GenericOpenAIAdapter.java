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
 * Universal OpenAI-compatible adapter in TAJ EGY.
 */
public final class GenericOpenAIAdapter implements AIProviderAdapter {
    private static final String TAG = "TajGeneric";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final ProviderType providerType;

    public GenericOpenAIAdapter(ProviderType providerType) {
        this.providerType = providerType != null ? providerType : ProviderType.GENERIC_OPENAI;
    }

    @Override
    public ProviderType getProviderType() {
        return providerType;
    }

    @Override
    public List<ModelInfo> getAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();
        models.add(new ModelInfo("default-model", "Default Model", providerType, 32000, 4096,
                EnumSet.of(ModelCapability.TEXT_GENERATION, ModelCapability.STREAMING), 0, 0));
        return Collections.unmodifiableList(models);
    }

    @Override
    public Set<ModelCapability> getCapabilities(String modelId) {
        return EnumSet.of(ModelCapability.TEXT_GENERATION, ModelCapability.STREAMING);
    }

    @Override
    public ConnectionTestResult testConnection(ProviderConfig config) {
        long start = SystemClock.elapsedRealtime();
        try {
            OkHttpClient client = NetworkClient.getProbeClient();
            String endpoint = config.getBaseUrl();
            if (endpoint.endsWith("/chat/completions")) {
                endpoint = endpoint.replace("/chat/completions", "/models");
            } else if (!endpoint.endsWith("/models")) {
                endpoint = endpoint.endsWith("/") ? endpoint + "models" : endpoint + "/models";
            }

            Request.Builder reqBuilder = new Request.Builder().url(endpoint).get();
            if (!config.getApiKey().isEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer " + config.getApiKey());
            }

            try (Response response = client.newCall(reqBuilder.build()).execute()) {
                long latency = SystemClock.elapsedRealtime() - start;
                if (response.isSuccessful()) {
                    return new ConnectionTestResult(true, response.code(), latency, "Reachable: HTTP " + response.code());
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
                    : "default-model";

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

            Request.Builder reqBuilder = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(root.toString(), JSON_MEDIA_TYPE));

            if (!config.getApiKey().isEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer " + config.getApiKey());
            }

            OkHttpClient client = NetworkClient.getInstance();
            Call call = client.newCall(reqBuilder.build());
            cancellationToken.attachCall(call);

            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    callback.onEvent(ChatStreamEvent.error("HTTP " + response.code() + " from server"));
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
                        Log.d(TAG, "Chunk parse notice: " + e.getMessage());
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
