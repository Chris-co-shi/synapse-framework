package com.indigo.synapse.oauth2.authorization.jwk;

import com.nimbusds.jose.jwk.RSAKey;

/**
 * JWT 签名 RSAKey 提供端口。
 */
@FunctionalInterface
public interface SigningKeyProvider {

    RSAKey signingKey();
}
