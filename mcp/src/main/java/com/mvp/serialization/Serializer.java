package com.mvp.serialization;


import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    // String-based operations
    <T> String serialize(T object);
    <T> T deserialize(String json, TypeReference<T> targetType);

    // Stream-based operations (useful for Stdio / socket transports)
    <T> void serialize(T object, OutputStream outputStream);
    <T> T deserialize(InputStream inputStream, TypeReference<T> targetType);
}