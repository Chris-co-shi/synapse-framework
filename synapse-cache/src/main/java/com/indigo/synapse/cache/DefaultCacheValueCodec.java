package com.indigo.synapse.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.UncheckedIOException;

public final class DefaultCacheValueCodec implements CacheValueCodec {

    private final ObjectMapper objectMapper;

    public DefaultCacheValueCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? defaultObjectMapper() : objectMapper;
    }

    @Override
    public String encode(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new UncheckedIOException(new java.io.IOException("failed to encode cache value", exception));
        }
    }

    @Override
    public <T> T decode(String value, Class<T> valueType) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, valueType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("failed to decode cache value", exception);
        }
    }

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
