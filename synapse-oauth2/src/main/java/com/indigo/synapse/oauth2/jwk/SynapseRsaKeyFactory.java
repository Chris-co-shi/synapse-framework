package com.indigo.synapse.oauth2.jwk;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

/**
 * RSA JWK 工厂。
 *
 * <p>该工厂用于创建 JWT RS256 签名所需的 RSAKey。{@link #generate(String)} 生成的是运行时临时密钥，
 * 只适合开发和测试；生产环境应由应用从安全的密钥来源构造 RSAKey Bean。</p>
 */
public final class SynapseRsaKeyFactory {

    private SynapseRsaKeyFactory() {
    }

    /**
     * 生成新的 RSAKey。
     *
     * @param keyId JWK key id
     * @return 包含公钥和私钥的 RSAKey
     */
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

    /**
     * 从已有 KeyPair 构造 RSAKey。
     *
     * <p>该方法适合应用从配置文件、证书库或外部密钥系统加载 KeyPair 后，转成 Nimbus RSAKey。</p>
     *
     * @param keyId JWK key id；为空时自动生成 UUID
     * @param keyPair RSA key pair
     * @return RSAKey
     */
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
