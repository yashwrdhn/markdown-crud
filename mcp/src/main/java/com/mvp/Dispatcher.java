package com.mvp;


import com.mvp.protocol.Response;
import com.mvp.protocol.Request;

import java.util.Map;

public class Dispatcher {

    private final ToolRegistry registry;

    public Dispatcher(ToolRegistry registry) {
        this.registry = registry;
    }

    public Response dispatch(Request request) {
        if (request == null) {
            return Response.error(null, -32600, "Invalid Request: Request cannot be null", null);
        }

        String method = request.method();
        Object id = request.id();

        try {
            return switch (method) {
                case "tools/list" -> handleToolsList(id);
                case "tools/call" -> invokeTool(id, request.params());
                default -> Response.error(id, -32601, "Method not found: " + method, null);
            };
        } catch (Exception e) {
            // Catch unexpected runtime errors during execution (JSON-RPC Internal Error)
            return Response.error(id, -32603, "Internal error: " + e.getMessage(), null);
        }
    }

    // Handles listing all registered tools
    private Response handleToolsList(Object id) {
        var tools = registry.list();
        return Response.success(id, Map.of("tools", tools));
    }

    // Handles calling a specific tool by name
    @SuppressWarnings("unchecked")
    private Response invokeTool(Object id, Object params) {
        if (!(params instanceof Map<?, ?> paramMap)) {
            return Response.error(id, -32602, "Invalid params: Expected JSON object with 'name' and 'arguments'", null);
        }

        String toolName = (String) paramMap.get("name");
        Object arguments = paramMap.get("arguments");
        if (toolName == null || toolName.isBlank()) {
            return Response.error(id, -32602, "Invalid params: Missing 'name' field", null);
        }

        // Look up the tool in the registry
        var optionalTool = registry.find(toolName);
        if (optionalTool.isEmpty()) {
            return Response.error(id, -32601, "Tool not found: " + toolName, null);
        }

        Tool tool = optionalTool.get();
        Object result = tool.execute(arguments);
        return Response.success(id, result);
    }
}