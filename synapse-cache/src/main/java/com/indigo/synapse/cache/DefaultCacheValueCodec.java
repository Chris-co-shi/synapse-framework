package com.indigo.synapse.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.UncheckedIOException;

/**
 * 默认缓存值编解码器。
 *
 * <p>该实现使用 Jackson 将对象序列化为字符串，再由 Redis 和 L1 本地缓存保存。消费方如果需要跨语言、
 * 压缩、加密、版本兼容或特殊类型处理，应提供自己的 {@link CacheValueCodec} Bean。</p>
 */
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
