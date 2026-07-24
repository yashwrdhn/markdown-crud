package com.mvp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvp.protocol.Request;
import com.mvp.protocol.Response;
import com.mvp.serialization.JacksonSerializer;
import com.mvp.serialization.Serializer;
import com.mvp.transport.HttpTransport;
import com.mvp.transport.Transport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {

//        Tool echo = new EchoTool();
//        ToolRegistry toolRegistry = new ToolRegistry();
//        toolRegistry.register(echo);
//        Dispatcher dispatcher = new Dispatcher(toolRegistry);
//
//        Serializer serializer = new JacksonSerializer();
//
//        String json = """
//                        {
//                          "jsonrpc":"2.0",
//                          "id":11,
//                          "method":"tools/call",
//                          "params":{
//                            "name":"echo",
//                            "arguments":{
//                              "message":"Hello MCP"
//                            }
//                          }
//                        }
//                        """;
//
//        Request request = serializer.deserialize(
//                json,
//                new TypeReference<Request>() {}
//        );
//
//
//        Response response = dispatcher.dispatch(request);
//
//        String responseJson = serializer.serialize(response);
//
//        System.out.println(responseJson);

        ToolRegistry registry = new ToolRegistry();
        registry.register(new EchoTool());

        Dispatcher dispatcher = new Dispatcher(registry);

        Serializer serializer = new JacksonSerializer();

        Transport transport =
                new HttpTransport(8080, dispatcher, serializer);

        transport.start();
    }
}
