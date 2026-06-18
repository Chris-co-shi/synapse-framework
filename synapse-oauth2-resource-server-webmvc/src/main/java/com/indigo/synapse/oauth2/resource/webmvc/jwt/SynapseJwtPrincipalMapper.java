package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.JwtClaimValues;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.springframework.security.oauth2.jwt.Jwt;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import java.util.Set;

/**
 * 将 JWT claims 映射为 Synapse Web 无关的已认证主体。
 *
 * <p>该类只承担模型转换，不负责 JWT 密码学验证，也不查询用户、客户端、角色或权限数据源。
 * roles 和 permissions 被视为签发方写入 token 的当前安全快照。</p>
 *
 * <p>主体类型必须通过 {@code principal_type} 明确区分：</p>
 * <ul>
 *     <li>{@code USER} 映射为 {@link AuthenticatedUser}，主体标识来自 {@code sub}。</li>
 *     <li>{@code CLIENT} 映射为 {@link AuthenticatedClient}，主体标识来自 {@code client_id}。</li>
 * </ul>
 *
 * <p>CLIENT 不会被伪装成 USER。该约束保证服务身份进入 OperationContext 时能够继续保持
 * SERVICE 语义，而不是错误地参与用户权限或审计逻辑。</p>
 */
public final class SynapseJwtPrincipalMapper {

    /**
     * 根据 {@code principal_type} 将 JWT 映射为用户主体或客户端主体。
     *
     * @param jwt 已通过 Resource Server 校验的 JWT
     * @return Synapse 已认证主体
     * @throws IllegalArgumentException 缺少必填 claim，或 principal_type 不受支持
     */
    public AuthenticatedPrincipal map(Jwt jwt) {
        JwtClaimAccessor claims = new SpringJwtClaimAccessor(jwt);
        String principalType = JwtClaimValues.requiredString(claims,
                SynapseJwtClaimNames.PRINCIPAL_TYPE);
        String tenantId = jwt.getClaimAsString(SynapseJwtClaimNames.TENANT_ID);
        Set<String> roles = JwtClaimValues.strings(
                claims,
                SynapseJwtClaimNames.ROLES
        );
        Set<String> permissions = JwtClaimValues.strings(
                claims,
                SynapseJwtClaimNames.PERMISSIONS
        );

        if (SynapsePrincipalType.CLIENT.name().equals(principalType)) {
            String clientId = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.CLIENT_ID);
            return new AuthenticatedClient(clientId, clientId, tenantId, roles, permissions);
        }

        if (SynapsePrincipalType.USER.name().equals(principalType)) {
            String userId = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.SUBJECT);
            String username = jwt.getClaimAsString(SynapseJwtClaimNames.PREFERRED_USERNAME);
            return new AuthenticatedUser(
                    userId,
                    username == null || username.isBlank() ? userId : username,
                    tenantId,
                    roles,
                    permissions
            );
        }

        throw new IllegalArgumentException("unsupported principal_type: " + principalType);
    }
}
