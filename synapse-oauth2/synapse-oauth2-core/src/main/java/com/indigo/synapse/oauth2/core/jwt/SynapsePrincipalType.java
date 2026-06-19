package com.indigo.synapse.oauth2.core.jwt;

/**
 * Synapse JWT 中 principal_type claim 的协议枚举。
 *
 * <p>该类型描述签发端与 Resource Server 之间的稳定 JWT 协议，
 * 不等同于 synapse-security 中认证完成后的领域主体类型。</p>
 */
public enum SynapsePrincipalType {

    /**
     * 用户身份。
     */
    USER,

    /**
     * 客户端或服务身份。
     */
    CLIENT
}
