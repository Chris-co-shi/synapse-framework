package com.indigo.synapse.security.header;

import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将 trusted-header 解析为 {@link AuthenticatedUser}。
 *
 * <p>解析器只处理可信入口已经注入的 Header 快照，不查询用户、角色或权限数据源。
 * roles 和 permissions 为空时保持为空集合，不自动伪造系统角色或默认权限。</p>
 */
public class TrustedHeaderAuthenticatedUserResolver {

    /**
     * 解析 trusted-header 主体。
     *
     * @param headers 请求头 Map，key 使用 {@link SecurityHeaders} 常量
     * @return 解析后的主体快照
     * @throws SynapseAuthenticationException 当必需 Header 缺失或为空
     */
    public TrustedHeaderPrincipal resolvePrincipal(Map<String, String> headers) {
        Map<String, String> checkedHeaders = requireHeaders(headers);
        String userId = required(checkedHeaders, SecurityHeaders.USER_ID);
        String username = required(checkedHeaders, SecurityHeaders.USERNAME);
        return new TrustedHeaderPrincipal(
                userId,
                username,
                trimToNull(checkedHeaders.get(SecurityHeaders.TENANT_ID)),
                parseCsv(checkedHeaders.get(SecurityHeaders.ROLES)),
                parseCsv(checkedHeaders.get(SecurityHeaders.PERMISSIONS)),
                trimToNull(checkedHeaders.get(SecurityHeaders.TRACE_ID)),
                trimToNull(checkedHeaders.get(SecurityHeaders.REQUEST_ID)),
                trimToNull(checkedHeaders.get(SecurityHeaders.SOURCE)),
                trimToNull(checkedHeaders.get(SecurityHeaders.TIMESTAMP)),
                trimToNull(checkedHeaders.get(SecurityHeaders.NONCE))
        );
    }

    /**
     * 解析 trusted-header 并转换为安全上下文使用的已认证用户主体。
     *
     * @param headers 请求头 Map
     * @return 已认证用户主体
     */
    public AuthenticatedUser resolveAuthenticatedUser(Map<String, String> headers) {
        TrustedHeaderPrincipal principal = resolvePrincipal(headers);
        return new AuthenticatedUser(
                principal.userId(),
                principal.username(),
                principal.tenantId(),
                principal.roles(),
                principal.permissions()
        );
    }

    private static Map<String, String> requireHeaders(Map<String, String> headers) {
        if (headers == null) {
            throw invalidHeader("trusted headers must not be null");
        }
        return headers;
    }

    private static String required(Map<String, String> headers, String name) {
        String value = trimToNull(headers.get(name));
        if (value == null) {
            throw invalidHeader(name + " must not be blank");
        }
        return value;
    }

    private static Set<String> parseCsv(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return Set.of();
        }
        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static SynapseAuthenticationException invalidHeader(String message) {
        return new SynapseAuthenticationException(CommonErrorCode.SECURITY_INVALID_TRUSTED_HEADER, message);
    }
}
