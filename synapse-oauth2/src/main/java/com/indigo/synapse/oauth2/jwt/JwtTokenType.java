package com.indigo.synapse.oauth2.jwt;

/**
 * JWT token 类型。
 *
 * <p>一阶段只提供 access token 类型，不实现 refresh token、authorization code、client credentials
 * 等完整 OAuth2 授权流程。</p>
 */
public enum JwtTokenType {
    /**
     * 访问令牌。
     */
    ACCESS_TOKEN
}
