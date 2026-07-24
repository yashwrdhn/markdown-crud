package com.mvp.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mvp.Dispatcher;
import com.mvp.protocol.Request;
import com.mvp.protocol.Response;
import com.mvp.serialization.Serializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpTransport implements Transport {

    private final HttpServer server;
    private final Dispatcher dispatcher;
    private final Serializer serializer;

    public HttpTransport(int port,
                         Dispatcher dispatcher,
                         Serializer serializer) throws IOException {

        this.dispatcher = dispatcher;
        this.serializer = serializer;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/mcp", new McpHandler());
    }

    @Override
    public void start() {
        server.start();
        System.out.println("MCP Server started on http://localhost:"
                + server.getAddress().getPort()
                + "/mcp");
    }

    @Override
    public void stop() {
        server.stop(0);
    }

    private class McpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Response response;

            try {

                Request request = serializer.deserialize(
                        exchange.getRequestBody(),
                        new TypeReference<Request>() {
                        });

                response = dispatcher.dispatch(request);

            } catch (Exception e) {

                response = Response.error(
                        null,
                        -32700,
                        "Parse error",
                        e.getMessage()
                );
            }

            byte[] bytes = serializer.serialize(response)
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders()
                    .set("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, bytes.length);

            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    }
}