package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * 将已经通过 {@code JwtDecoder} 校验的 JWT 转换为 Spring Security Authentication。
 *
 * <p>该转换器位于 Spring Security Resource Server 与 Synapse Security 之间：它保留 Spring Security
 * 所需的 authorities，同时通过 {@link SynapseJwtPrincipalMapper} 生成 Web 无关的
 * {@link AuthenticatedPrincipal}，并将两者封装进 {@link SynapseJwtAuthenticationToken}。</p>
 *
 * <p>职责边界：</p>
 * <ul>
 *     <li>不解析 HTTP Bearer Header，该职责属于 Spring Security Filter。</li>
 *     <li>不验证 JWT 签名、issuer、audience 或有效期，该职责属于 {@code JwtDecoder} 和 validators。</li>
 *     <li>不把主体写入 Synapse CurrentPrincipalContext，该职责属于后续 Bridge Filter。</li>
 *     <li>不查询用户、角色或权限数据库，claims 被视为当前 token 的安全快照。</li>
 * </ul>
 *
 * <p>主链路：{@code Jwt -> SynapseJwtAuthenticationToken -> Spring SecurityContextHolder}。</p>
 */
public final class SynapseJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final SynapseJwtPrincipalMapper principalMapper;
    private final SynapseJwtGrantedAuthoritiesConverter authoritiesConverter;

    /**
     * 使用框架默认的主体映射器和 authorities 转换器。
     */
    public SynapseJwtAuthenticationConverter() {
        this(new SynapseJwtPrincipalMapper(), new SynapseJwtGrantedAuthoritiesConverter());
    }

    /**
     * 创建可替换映射策略的 JWT Authentication 转换器。
     *
     * @param principalMapper 将 JWT claims 映射为 Synapse 主体
     * @param authoritiesConverter 将 JWT claims 映射为 Spring Security authorities
     */
    public SynapseJwtAuthenticationConverter(
            SynapseJwtPrincipalMapper principalMapper,
            SynapseJwtGrantedAuthoritiesConverter authoritiesConverter) {
        this.principalMapper = principalMapper;
        this.authoritiesConverter = authoritiesConverter;
    }

    /**
     * 将可信 JWT 转换为同时包含 Spring authorities 与 Synapse principal 的 Authentication。
     *
     * @param jwt 已由 Resource Server {@code JwtDecoder} 完成校验的 JWT
     * @return 可写入 Spring SecurityContextHolder 的 Authentication
     */
    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        AuthenticatedPrincipal principal = principalMapper.map(jwt);
        return new SynapseJwtAuthenticationToken(
                jwt,
                authoritiesConverter.convert(jwt),
                principal,
                new TokenMetadata(jwt.getId(), jwt.getClaimAsString(SynapseJwtClaimNames.ISSUER))
        );
    }
}
