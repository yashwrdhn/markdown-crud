package com.mvp;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ToolRegistry {

    private final ConcurrentMap<String, Tool> tools = new ConcurrentHashMap<>();

    /**
     * Registers a tool. Overwrites if a tool with the same name already exists.
     */
    public void register(Tool tool) {
        if (tool == null || tool.getName().isBlank()) {
            throw new IllegalArgumentException("Tool and tool name must not be null or blank");
        }
        tools.put(tool.getName(), tool);
    }

    /**
     * Unregisters a tool by name.
     * @return true if a tool was removed, false if it didn't exist.
     */
    public boolean unregister(String name) {
        if (name == null) return false;
        return tools.remove(name) != null;
    }

    /**
     * Finds a registered tool by its name.
     */
    public Optional<Tool> find(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * Returns an immutable snapshot list of all registered tools.
     */
    public List<Tool> list() {
        return List.copyOf(tools.values());
    }

    /**
     * Returns the current number of registered tools.
     */
    public int size() {
        return tools.size();
    }
}