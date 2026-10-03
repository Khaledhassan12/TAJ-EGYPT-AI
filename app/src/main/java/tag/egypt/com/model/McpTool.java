package tag.egypt.com.model;

import java.util.Objects;

/**
 * Individual tool exposed by an external MCP server or built-in registry in TAJ EGY.
 */
public final class McpTool {
    private final String id;
    private final String serverId;
    private final String name;
    private final String description;
    private final String inputSchemaJson;
    private final boolean isEnabled;
    private final boolean requiresApproval;

    public McpTool(
            String id,
            String serverId,
            String name,
            String description,
            String inputSchemaJson,
            boolean isEnabled,
            boolean requiresApproval
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.serverId = serverId != null ? serverId : "";
        this.name = Objects.requireNonNull(name, "tool name cannot be null");
        this.description = description != null ? description : "";
        this.inputSchemaJson = inputSchemaJson != null ? inputSchemaJson : "{}";
        this.isEnabled = isEnabled;
        this.requiresApproval = requiresApproval;
    }

    public String getId() {
        return id;
    }

    public String getServerId() {
        return serverId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getInputSchemaJson() {
        return inputSchemaJson;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }
}
