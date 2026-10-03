package tag.egypt.com.mcp;

import tag.egypt.com.model.McpServer;
import tag.egypt.com.model.McpTool;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses Model Context Protocol (MCP) JSON configuration files and JSON-RPC 2.0 payloads in TAJ EGY.
 */
public final class McpProtocolParser {

    private McpProtocolParser() {
        // Utility
    }

    public static List<McpServer> parseMcpConfigJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new IllegalArgumentException("MCP JSON cannot be empty");
        }

        try {
            JSONObject root = new JSONObject(jsonString.trim());
            List<McpServer> servers = new ArrayList<>();

            JSONObject serversObj = root.has("mcpServers") ? root.getJSONObject("mcpServers") : root;

            for (java.util.Iterator<String> it = serversObj.keys(); it.hasNext(); ) {
                String serverName = it.next();
                JSONObject serverJson = serversObj.getJSONObject(serverName);

                String url = serverJson.optString("url", "");
                String command = serverJson.optString("command", "");
                String desc = serverJson.optString("description", "Imported MCP Server");

                McpServer.Transport transport;
                if (!command.isEmpty()) {
                    transport = McpServer.Transport.STDIO;
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    transport = McpServer.Transport.SSE;
                } else {
                    transport = McpServer.Transport.HTTP;
                }

                JSONObject headers = serverJson.optJSONObject("headers");
                String headersJson = headers != null ? headers.toString() : "{}";

                servers.add(new McpServer(
                        "mcp_" + java.util.UUID.randomUUID().toString().substring(0, 8),
                        serverName,
                        url,
                        transport,
                        desc,
                        transport.isSupportedOnAndroid(),
                        false,
                        "",
                        headersJson,
                        0
                ));
            }

            return Collections.unmodifiableList(servers);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse MCP JSON: " + e.getMessage(), e);
        }
    }

    public static List<McpTool> parseToolsList(String serverId, String jsonRpcResponse) {
        try {
            JSONObject root = new JSONObject(jsonRpcResponse);
            JSONObject result = root.optJSONObject("result");
            if (result == null) return Collections.emptyList();

            JSONArray toolsArray = result.optJSONArray("tools");
            if (toolsArray == null) return Collections.emptyList();

            List<McpTool> tools = new ArrayList<>();
            for (int i = 0; i < toolsArray.length(); i++) {
                JSONObject t = toolsArray.getJSONObject(i);
                String name = t.optString("name", "tool_" + i);
                String desc = t.optString("description", "");
                JSONObject schema = t.optJSONObject("inputSchema");
                String schemaStr = schema != null ? schema.toString() : "{}";

                tools.add(new McpTool(
                        serverId + "_" + name,
                        serverId,
                        name,
                        desc,
                        schemaStr,
                        true,
                        true
                ));
            }
            return Collections.unmodifiableList(tools);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
