package com.indigo.synapse.security.jwk;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

public final class SynapseRsaKeyFactory {

    private SynapseRsaKeyFactory() {
    }

    public static RSAKey generate(String keyId) {
        SecurityKeyPolicy.validateSigningKeyId(keyId, false);
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
                .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .issueTime(java.util.Date.from(Instant.now()))
                .build();
    }
}
