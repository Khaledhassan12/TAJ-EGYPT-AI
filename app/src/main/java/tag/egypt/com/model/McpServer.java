package tag.egypt.com.model;

/**
 * Model Context Protocol (MCP) server configuration in TAJ EGY.
 */
public final class McpServer {

    public enum Transport {
        SSE("Server-Sent Events (SSE)"),
        HTTP("HTTP Streaming"),
        STDIO("Local Stdio (Desktop only - Unsupported on Android)");

        private final String description;

        Transport(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isSupportedOnAndroid() {
            return this != STDIO;
        }
    }

    private final String id;
    private final String name;
    private final String serverUrl;
    private final Transport transport;
    private final String description;
    private final boolean isEnabled;
    private final boolean isAutoConnect;
    private final String authToken;
    private final String customHeadersJson;
    private final int toolCount;

    public McpServer(
            String id,
            String name,
            String serverUrl,
            Transport transport,
            String description,
            boolean isEnabled,
            boolean isAutoConnect,
            String authToken,
            String customHeadersJson,
            int toolCount
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.name = name != null ? name : "MCP Server";
        this.serverUrl = serverUrl != null ? serverUrl.trim() : "";
        this.transport = transport != null ? transport : Transport.SSE;
        this.description = description != null ? description : "";
        this.isEnabled = isEnabled;
        this.isAutoConnect = isAutoConnect;
        this.authToken = authToken != null ? authToken : "";
        this.customHeadersJson = customHeadersJson != null ? customHeadersJson : "{}";
        this.toolCount = toolCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public Transport getTransport() {
        return transport;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isAutoConnect() {
        return isAutoConnect;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getCustomHeadersJson() {
        return customHeadersJson;
    }

    public int getToolCount() {
        return toolCount;
    }

    public McpServer copyWithToolCount(int count) {
        return new McpServer(
                this.id,
                this.name,
                this.serverUrl,
                this.transport,
                this.description,
                this.isEnabled,
                this.isAutoConnect,
                this.authToken,
                this.customHeadersJson,
                count
        );
    }

    public McpServer copyWithEnabled(boolean enabled) {
        return new McpServer(
                this.id,
                this.name,
                this.serverUrl,
                this.transport,
                this.description,
                enabled,
                this.isAutoConnect,
                this.authToken,
                this.customHeadersJson,
                this.toolCount
        );
    }
}
