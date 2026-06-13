package com.indigo.synapse.cache;

/**
 * 缓存值编解码端口。
 *
 * <p>缓存存储层只保存字符串，该端口负责对象与字符串之间转换。实现应保持编码和
 * 解码配置一致，避免跨服务版本读取缓存时出现不兼容。</p>
 */
public interface CacheValueCodec {

    /**
     * 编码缓存值；值为 {@code null} 时可返回 {@code null}。
     */
    String encode(Object value);

    /**
     * 解码缓存值。
     */
    <T> T decode(String value, Class<T> valueType);
}
