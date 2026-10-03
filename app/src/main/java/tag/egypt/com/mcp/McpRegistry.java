package tag.egypt.com.mcp;

import tag.egypt.com.model.McpServer;
import tag.egypt.com.model.McpTool;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry holding active MCP servers and cached tool signatures in TAJ EGY.
 */
public final class McpRegistry {
    private static volatile McpRegistry instance;
    private final List<McpServer> registeredServers = new CopyOnWriteArrayList<>();
    private final List<McpTool> availableTools = new CopyOnWriteArrayList<>();

    private McpRegistry() {
        // Singleton
    }

    public static McpRegistry getInstance() {
        if (instance == null) {
            synchronized (McpRegistry.class) {
                if (instance == null) {
                    instance = new McpRegistry();
                }
            }
        }
        return instance;
    }

    public void updateServers(List<McpServer> servers) {
        registeredServers.clear();
        if (servers != null) {
            registeredServers.addAll(servers);
        }
    }

    public List<McpServer> getRegisteredServers() {
        return Collections.unmodifiableList(registeredServers);
    }

    public void registerTools(List<McpTool> tools) {
        if (tools != null) {
            availableTools.addAll(tools);
        }
    }

    public List<McpTool> getAvailableTools() {
        return Collections.unmodifiableList(availableTools);
    }
}
