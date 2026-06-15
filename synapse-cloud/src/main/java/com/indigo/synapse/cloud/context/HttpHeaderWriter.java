package com.indigo.synapse.cloud.context;

/**
 * 轻量 HTTP Header 写入端口。
 *
 * <p>实现方负责把 Header 写入 Feign、测试 Map 或其他 HTTP client。调用方必须保证不写入业务敏感字段。</p>
 */
@FunctionalInterface
public interface HttpHeaderWriter {

    /**
     * 写入 Header。
     *
     * @param name Header 名称
     * @param value Header 值
     */
    void write(String name, String value);
}
