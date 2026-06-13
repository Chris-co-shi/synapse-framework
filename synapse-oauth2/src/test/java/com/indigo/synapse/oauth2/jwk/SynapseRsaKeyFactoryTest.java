package com.indigo.synapse.oauth2.jwk;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SynapseRsaKeyFactoryTest {

    @Test
    void shouldGenerateRsaJwkForSigning() throws Exception {
        RSAKey rsaKey = SynapseRsaKeyFactory.generate("kid-test");

        assertEquals("kid-test", rsaKey.getKeyID());
        assertEquals(com.nimbusds.jose.jwk.KeyUse.SIGNATURE, rsaKey.getKeyUse());
        assertNotNull(rsaKey.toRSAPublicKey());
        assertNotNull(rsaKey.toRSAPrivateKey());
    }
}
