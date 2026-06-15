package com.indigo.synapse.webmvc.trace;

import java.util.UUID;

/**
 * Web traceId 生成器。
 *
 * <p>默认实现基于 UUID，返回 32 位无连字符字符串。它只用于 Web 层请求追踪，
 * 不承诺有序性，也不作为业务主键或业务单号。</p>
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    /**
     * 生成新的 traceId。
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
