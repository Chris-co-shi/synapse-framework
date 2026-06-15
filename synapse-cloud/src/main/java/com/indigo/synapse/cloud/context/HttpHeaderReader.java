package com.indigo.synapse.cloud.context;

/**
 * 轻量 HTTP Header 读取端口。
 *
 * <p>该端口用于让 cloud codec 适配 Feign、测试 Map 或后续其他 HTTP 客户端，而不直接绑定某个 Web 栈。</p>
 */
@FunctionalInterface
public interface HttpHeaderReader {

    /**
     * 判断目标 Header 是否已经存在。
     *
     * @param name Header 名称
     * @return Header 已存在时返回 true
     */
    boolean contains(String name);
}
