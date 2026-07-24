import com.fasterxml.jackson.core.type.TypeReference;
import com.mvp.Dispatcher;
import com.mvp.EchoTool;
import com.mvp.ToolRegistry;
import com.mvp.protocol.Request;
import com.mvp.protocol.Response;
import com.mvp.serialization.JacksonSerializer;
import com.mvp.serialization.Serializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class MCPRuntimeTest {

    @Test
    void shouldDeserializeDispatchAndSerialize() {

        Serializer serializer = new JacksonSerializer();

        ToolRegistry registry = new ToolRegistry();
        registry.register(new EchoTool());

        Dispatcher dispatcher = new Dispatcher(registry);

        String json = """
    {
      "jsonrpc":"2.0",
      "id":1,
      "method":"tools/call",
      "params":{
        "name":"echo",
        "arguments":{
          "message":"Hello"
        }
      }
    }
    """;

        Request request = serializer.deserialize(
                json,
                new TypeReference<Request>() {}
        );

        Response response = dispatcher.dispatch(request);

        String output = serializer.serialize(response);

        assertTrue(output.contains("Hello"));
    }

}
