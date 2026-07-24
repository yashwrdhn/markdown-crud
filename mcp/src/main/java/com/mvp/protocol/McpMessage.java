package com.mvp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface McpMessage
        permits Request, Notification, Response {

    String jsonrpc();

}