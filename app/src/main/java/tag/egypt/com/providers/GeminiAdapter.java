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
 * Adapter for Google Gemini models via Google AI REST API in TAJ EGY.
 */
public final class GeminiAdapter implements AIProviderAdapter {
    private static final String TAG = "TajGemini";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final List<ModelInfo> presetModels;

    public GeminiAdapter() {
        List<ModelInfo> models = new ArrayList<>();
        Set<ModelCapability> caps = EnumSet.of(
                ModelCapability.TEXT_GENERATION,
                ModelCapability.STREAMING,
                ModelCapability.VISION,
                ModelCapability.FILE_INPUT,
                ModelCapability.TOOL_CALLING
        );

        models.add(new ModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro (Flagship Multimodal)", ProviderType.GEMINI, 1000000, 8192, caps, 1.25, 5.00));
        models.add(new ModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash (Ultra-Fast & Smart)", ProviderType.GEMINI, 1000000, 8192, caps, 0.075, 0.30));
        models.add(new ModelInfo("gemini-1.5-pro", "Gemini 1.5 Pro (Long Context 2M)", ProviderType.GEMINI, 2000000, 8192, caps, 1.25, 5.00));
        models.add(new ModelInfo("gemini-1.5-flash", "Gemini 1.5 Flash (Lightweight)", ProviderType.GEMINI, 1000000, 8192, caps, 0.075, 0.30));
        this.presetModels = Collections.unmodifiableList(models);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GEMINI;
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
            String endpoint = config.getBaseUrl() + "/v1beta/models?key=" + config.getApiKey();

            Request request = new Request.Builder()
                    .url(endpoint)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                long latency = SystemClock.elapsedRealtime() - start;
                if (response.isSuccessful()) {
                    return new ConnectionTestResult(true, response.code(), latency, "Authenticated successfully with Google Gemini.");
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
                    : "gemini-1.5-flash";

            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                JSONObject sys = new JSONObject();
                JSONArray sysParts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", systemPrompt.trim());
                sysParts.put(part);
                sys.put("parts", sysParts);
                root.put("systemInstruction", sys);
            }

            JSONArray contents = new JSONArray();
            for (ChatMessage msg : history) {
                if (msg.getRole() == ChatMessage.Role.SYSTEM) continue;
                JSONObject content = new JSONObject();
                content.put("role", msg.getRole() == ChatMessage.Role.ASSISTANT ? "model" : "user");
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", msg.getContent());
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
            }
            root.put("contents", contents);

            String endpoint = config.getBaseUrl() + "/v1beta/models/" + model + ":streamGenerateContent?key=" + config.getApiKey() + "&alt=sse";

            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(root.toString(), JSON_MEDIA_TYPE))
                    .build();

            OkHttpClient client = NetworkClient.getInstance();
            Call call = client.newCall(request);
            cancellationToken.attachCall(call);

            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    String err = "HTTP " + response.code() + " from Gemini";
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
                        if (json.has("candidates")) {
                            JSONArray candidates = json.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                JSONObject cand = candidates.getJSONObject(0);
                                if (cand.has("content") && cand.getJSONObject("content").has("parts")) {
                                    JSONArray parts = cand.getJSONObject("content").getJSONArray("parts");
                                    for (int i = 0; i < parts.length(); i++) {
                                        JSONObject partObj = parts.getJSONObject(i);
                                        if (partObj.has("text")) {
                                            callback.onEvent(ChatStreamEvent.token(partObj.getString("text")));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Gemini chunk parse notice: " + e.getMessage());
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
