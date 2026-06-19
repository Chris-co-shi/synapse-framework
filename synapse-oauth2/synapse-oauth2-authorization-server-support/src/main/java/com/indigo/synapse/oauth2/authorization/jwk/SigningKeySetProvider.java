package com.indigo.synapse.oauth2.authorization.jwk;

import com.nimbusds.jose.jwk.JWKSet;

/**
 * JWKSet 提供端口。
 */
@FunctionalInterface
public interface SigningKeySetProvider {

    JWKSet signingKeySet();
}
