package tag.egypt.com.network;

import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * Shared high-performance networking engine for TAJ EGY.
 */
public final class NetworkClient {
    private static volatile OkHttpClient okHttpClient;

    private NetworkClient() {
        // Utility singleton
    }

    public static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            synchronized (NetworkClient.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();
                }
            }
        }
        return okHttpClient;
    }

    public static OkHttpClient getProbeClient() {
        return getInstance().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
    }
}
