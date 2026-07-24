package com.mvp.serialization;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;

public class JacksonSerializer implements Serializer {

    private final ObjectMapper mapper;

    public JacksonSerializer() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public JacksonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> String serialize(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize object to JSON string", e);
        }
    }

    @Override
    public <T> T deserialize(String json, TypeReference<T> targetType) {
        try {
            return mapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize JSON string into " + targetType.getType().getTypeName(), e);
        }
    }

    @Override
    public <T> void serialize(T object, OutputStream outputStream) {
        try {
            mapper.writeValue(outputStream, object);
        } catch (Exception e) {
            throw new SerializationException("Failed to write serialized JSON to OutputStream", e);
        }
    }

    @Override
    public <T> T deserialize(InputStream inputStream, TypeReference<T> targetType) {
        try {
            return mapper.readValue(inputStream, targetType);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize InputStream into " + targetType.getType().getTypeName(), e);
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
