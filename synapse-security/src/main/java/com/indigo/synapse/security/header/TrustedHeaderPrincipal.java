package com.indigo.synapse.security.header;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * trusted-header 解析后的认证主体快照。
 *
 * <p>该模型保存从可信请求头中恢复出来的轻量身份、角色、权限和追踪字段。它是 Header 解析阶段的中间模型，
 * 之后可转换为 {@link com.indigo.synapse.security.context.AuthenticatedUser} 写入 SecurityContext。</p>
 *
 * <p>该模型不包含认证令牌、OAuth2 对象、组织部门、菜单或扩展授权规则。</p>
 *
 * @param userId 用户稳定标识
 * @param username 用户展示名或登录名
 * @param tenantId 租户标识
 * @param roles 角色快照
 * @param permissions 权限快照
 * @param traceId 链路追踪标识
 * @param requestId 请求标识
 * @param source 调用来源
 * @param timestamp epoch millis 时间戳字符串
 * @param nonce 随机数
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
