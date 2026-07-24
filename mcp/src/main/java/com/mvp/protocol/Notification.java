package com.mvp.protocol;

public record Notification(
        String jsonrpc,
        String method,
        Object params
) implements McpMessage {
    public Notification(String method, Object params) {
        this("2.0", method, params);
    }
}
