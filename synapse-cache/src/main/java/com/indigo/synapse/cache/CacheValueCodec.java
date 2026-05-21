package com.indigo.synapse.cache;

public interface CacheValueCodec {

    String encode(Object value);

    <T> T decode(String value, Class<T> valueType);
}
