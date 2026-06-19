package com.indigo.synapse.oauth2.resource.core;

import com.indigo.synapse.oauth2.core.jwt.JwtClaimValues;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;

import java.util.Set;

/**
 * 将已验证 JWT claim 映射为协议中立的 Synapse 主体。
 *
 * <p>该映射器不验证签名、不查询用户或权限数据库，也不写入当前主体上下文。roles 与
 * permissions 只是 token 中的当前快照。</p>
 */
public final class SynapsePrincipalClaimMapper {

    /**
     * 映射主体。
     *
     * @param claims JWT claim 读取端口
     * @return 用户或客户端主体
     * @throws IllegalArgumentException 主体类型未知或必填 claim 缺失
     */
    public AuthenticatedPrincipal map(JwtClaimAccessor claims) {
        String principalType = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.PRINCIPAL_TYPE);
        String tenantId = claims.string(SynapseJwtClaimNames.TENANT_ID).orElse(null);
        Set<String> roles = JwtClaimValues.strings(claims, SynapseJwtClaimNames.ROLES);
        Set<String> permissions = JwtClaimValues.strings(claims, SynapseJwtClaimNames.PERMISSIONS);
        if (SynapsePrincipalType.CLIENT.name().equals(principalType)) {
            String clientId = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.CLIENT_ID);
            return new AuthenticatedClient(clientId, clientId, tenantId, roles, permissions);
        }
        if (SynapsePrincipalType.USER.name().equals(principalType)) {
            String userId = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.SUBJECT);
            String username = claims.string(SynapseJwtClaimNames.PREFERRED_USERNAME)
                    .filter(value -> !value.isBlank())
                    .orElse(userId);
            return new AuthenticatedUser(userId, username, tenantId, roles, permissions);
        }
        throw new IllegalArgumentException("unsupported principal_type: " + principalType);
    }
}
