package tag.egypt.com.mcp;

import android.os.SystemClock;
import android.util.Log;

import tag.egypt.com.model.McpServer;
import tag.egypt.com.model.McpTool;
import tag.egypt.com.network.NetworkClient;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Communicates with remote Model Context Protocol (MCP) servers in TAJ EGY.
 */
public final class McpClient {
    private static final String TAG = "TajMcpClient";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private McpClient() {
        // Utility
    }

    public static List<McpTool> fetchTools(McpServer server) {
        if (!server.getTransport().isSupportedOnAndroid()) {
            Log.w(TAG, "Server transport " + server.getTransport() + " not executable on Android");
            return Collections.emptyList();
        }

        try {
            OkHttpClient client = NetworkClient.getProbeClient();

            JSONObject rpc = new JSONObject();
            rpc.put("jsonrpc", "2.0");
            rpc.put("id", 1);
            rpc.put("method", "tools/list");
            rpc.put("params", new JSONObject());

            Request.Builder req = new Request.Builder()
                    .url(server.getServerUrl())
                    .post(RequestBody.create(rpc.toString(), JSON_MEDIA_TYPE));

            if (!server.getAuthToken().isEmpty()) {
                req.addHeader("Authorization", "Bearer " + server.getAuthToken());
            }

            try (Response response = client.newCall(req.build()).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    return McpProtocolParser.parseToolsList(server.getId(), json);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "MCP tool discovery notice: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public static String executeTool(McpServer server, String toolName, String argumentsJson) throws IOException {
        try {
            OkHttpClient client = NetworkClient.getInstance();

            JSONObject rpc = new JSONObject();
            rpc.put("jsonrpc", "2.0");
            rpc.put("id", SystemClock.elapsedRealtime());
            rpc.put("method", "tools/call");

            JSONObject params = new JSONObject();
            params.put("name", toolName);
            params.put("arguments", new JSONObject(argumentsJson));
            rpc.put("params", params);

            Request.Builder req = new Request.Builder()
                    .url(server.getServerUrl())
                    .post(RequestBody.create(rpc.toString(), JSON_MEDIA_TYPE));

            if (!server.getAuthToken().isEmpty()) {
                req.addHeader("Authorization", "Bearer " + server.getAuthToken());
            }

            try (Response response = client.newCall(req.build()).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP " + response.code() + " from MCP server");
                }
                return response.body() != null ? response.body().string() : "{}";
            }
        } catch (Exception e) {
            throw new IOException("MCP tool execution failed: " + e.getMessage(), e);
        }
    }
}
