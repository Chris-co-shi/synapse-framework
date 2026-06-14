package com.indigo.synapse.security.header;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * trusted-header 解析后的认证主体快照。
 *
 * <p>该模型只保存业务服务恢复安全上下文所需的轻量字段，不包含认证令牌、
 * 具体授权协议对象、组织部门或扩展授权属性。</p>
 */
public record TrustedHeaderPrincipal(
        String userId,
        String username,
        String tenantId,
        Set<String> roles,
        Set<String> permissions,
        String traceId,
        String requestId,
        String source,
        String timestamp,
        String nonce
) {

    public TrustedHeaderPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
    }
}
