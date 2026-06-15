package com.indigo.synapse.cloud.security;

import java.util.Collection;
import java.util.Map;

/**
 * 内部调用签名请求上下文。
 *
 * <p>该类型只暴露签名所需的技术元数据，不包含用户角色、权限、原始 token 或业务数据。</p>
 *
 * @param method HTTP 方法
 * @param url 请求 URL
 * @param headers 当前请求 Header 快照
 */
public record InternalCallSignRequest(
        String method,
        String url,
        Map<String, Collection<String>> headers
) {
    public InternalCallSignRequest {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }
}
