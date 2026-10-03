package tag.egypt.com.network;

import android.util.Log;

import java.io.IOException;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Robust line-by-line Server-Sent Events (SSE) reader for streaming HTTP responses in TAJ EGY.
 */
public final class SseStreamReader {
    private static final String TAG = "TajSseReader";

    public interface SseLineConsumer {
        void accept(String eventType, String data) throws Exception;
    }

    private SseStreamReader() {
        // Utility
    }

    public static void read(Response response, CancellationToken cancellationToken, SseLineConsumer consumer) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("Empty response body from provider");
        }

        try (BufferedSource source = body.source()) {
            String currentEvent = "message";
            while (!source.exhausted()) {
                if (cancellationToken != null && cancellationToken.isCancelled()) {
                    break;
                }

                String line = source.readUtf8Line();
                if (line == null) break;

                line = line.trim();
                if (line.isEmpty() || line.startsWith(":")) {
                    continue;
                }

                if (line.startsWith("event:")) {
                    currentEvent = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    if ("[DONE]".equalsIgnoreCase(data)) {
                        break;
                    }
                    try {
                        consumer.accept(currentEvent, data);
                    } catch (Exception e) {
                        Log.w(TAG, "Error handling SSE chunk: " + e.getMessage());
                    }
                }
            }
        }
    }
}
