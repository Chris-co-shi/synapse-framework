package com.indigo.synapse.oauth2.authorization.jwk;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * RSA JWK 工厂。
 */
public final class SynapseRsaKeyFactory {

    private SynapseRsaKeyFactory() {
    }

    public static RSAKey generate(String keyId) {
        SigningKeyPolicy.validateSigningKeyId(keyId, false);
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return fromKeyPair(keyId, generator.generateKeyPair());
        } catch (Exception ex) {
            throw new IllegalStateException("failed to generate RSA signing key", ex);
        }
    }

    public static RSAKey fromKeyPair(String keyId, KeyPair keyPair) {
        if (keyPair == null) {
            throw new IllegalArgumentException("keyPair must not be null");
        }
        String resolvedKeyId = keyId == null || keyId.isBlank() ? UUID.randomUUID().toString() : keyId;
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(resolvedKeyId)
                .keyUse(KeyUse.SIGNATURE)
                .issueTime(Date.from(Instant.now()))
                .build();
    }
}
