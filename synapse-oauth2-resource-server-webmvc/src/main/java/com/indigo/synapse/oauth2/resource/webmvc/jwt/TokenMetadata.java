package com.indigo.synapse.oauth2.resource.webmvc.jwt;

/**
 * 当前认证 token 的轻量元数据。
 *
 * @param tokenId JWT jti
 * @param issuer issuer
 */
public record TokenMetadata(String tokenId, String issuer) {
}
