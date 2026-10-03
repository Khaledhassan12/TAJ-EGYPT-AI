package tag.egypt.com.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry managing available tools across built-in utilities and MCP services in TAJ EGY.
 */
public final class ToolRegistry {
    private static volatile ToolRegistry instance;
    private final Map<String, Tool> tools = new HashMap<>();

    private ToolRegistry() {
        registerTool(new BuiltInTools.CalculatorTool());
        registerTool(new BuiltInTools.DateTimeTool());
        registerTool(new BuiltInTools.TokenEstimatorTool());
        registerTool(new BuiltInTools.JsonFormatterTool());
    }

    public static ToolRegistry getInstance() {
        if (instance == null) {
            synchronized (ToolRegistry.class) {
                if (instance == null) {
                    instance = new ToolRegistry();
                }
            }
        }
        return instance;
    }

    public void registerTool(Tool tool) {
        if (tool != null) {
            tools.put(tool.getName(), tool);
        }
    }

    public Tool getTool(String name) {
        return tools.get(name);
    }

    public List<Tool> getAllTools() {
        return Collections.unmodifiableList(new ArrayList<>(tools.values()));
    }
}
