package com.mvp.protocol;

public record Request(
        String jsonrpc,
        Object id,
        String method,
        Object params
) implements McpMessage {
    public Request(Object id, String method, Object params) {
        this("2.0", id, method, params);
    }
}
