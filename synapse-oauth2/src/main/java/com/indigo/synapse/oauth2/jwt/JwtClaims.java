package com.indigo.synapse.oauth2.jwt;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Synapse JWT 载荷快照。
 *
 * <p>该模型只封装当前模块签发和校验 JWT 所需的标准 claims 以及 token_type，不表达业务用户、
 * 角色、权限、菜单或组织结构。业务系统如需扩展自定义 claims，应在上层服务中自行扩展签发逻辑。</p>
 *
 * @param issuer 签发方
 * @param subject 主体标识，通常是用户 ID 或客户端 ID
 * @param audience 受众集合
 * @param tokenId token 唯一标识，对应 JWT jti
 * @param tokenType token 类型
 * @param issuedAt 签发时间
 * @param expiresAt 过期时间，必须晚于 issuedAt
 */
public record JwtClaims(
        String issuer,
        String subject,
        Set<String> audience,
        String tokenId,
        JwtTokenType tokenType,
        Instant issuedAt,
        Instant expiresAt
) {

    public JwtClaims {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("tokenId must not be blank");
        }
        if (tokenType == null) {
            throw new IllegalArgumentException("tokenType must not be null");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("issuedAt must not be null");
        }
        if (expiresAt == null || !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        audience = audience == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(audience));
    }

    /**
     * 判断当前 token 是否已过期。
     *
     * @param now 当前时间
     * @return 当前时间不早于 expiresAt 时返回 true
     */
    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return !now.isBefore(expiresAt);
    }
}
