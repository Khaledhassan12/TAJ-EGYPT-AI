package tag.egypt.com.providers;

import tag.egypt.com.model.ChatMessage;
import tag.egypt.com.model.ModelCapability;
import tag.egypt.com.model.ModelInfo;
import tag.egypt.com.model.ProviderConfig;
import tag.egypt.com.model.ProviderType;
import tag.egypt.com.network.CancellationToken;
import tag.egypt.com.network.StreamCallback;

import java.util.List;
import java.util.Set;

/**
 * Common contract implemented by all AI Provider adapters in TAJ EGY.
 */
public interface AIProviderAdapter {

    ProviderType getProviderType();

    List<ModelInfo> getAvailableModels();

    Set<ModelCapability> getCapabilities(String modelId);

    ConnectionTestResult testConnection(ProviderConfig config);

    void generateStream(
            ProviderConfig config,
            List<ChatMessage> history,
            String systemPrompt,
            StreamCallback callback,
            CancellationToken cancellationToken
    );

    final class ConnectionTestResult {
        private final boolean success;
        private final int httpStatus;
        private final long latencyMs;
        private final String message;

        public ConnectionTestResult(boolean success, int httpStatus, long latencyMs, String message) {
            this.success = success;
            this.httpStatus = httpStatus;
            this.latencyMs = latencyMs;
            this.message = message != null ? message : "";
        }

        public boolean isSuccess() {
            return success;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public String getMessage() {
            return message;
        }
    }
}
