package com.mvp.protocol;

public record Response(
        String jsonrpc,
        Object id,
        Object result,
        Error error
) implements McpMessage {
    public static Response success(Object id, Object result) {
        return new Response("2.0", id, result, null);
    }

    public static Response error(Object id, int code, String message, Object data) {
        return new Response("2.0", id, null, new Error(code, message, data));
    }
}
