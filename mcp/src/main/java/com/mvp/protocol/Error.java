package com.mvp.protocol;

public record Error(
        int code,
        String message,
        Object data
) {}
