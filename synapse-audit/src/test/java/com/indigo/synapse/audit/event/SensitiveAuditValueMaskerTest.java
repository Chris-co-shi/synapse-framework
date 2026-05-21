package com.indigo.synapse.audit.event;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveAuditValueMaskerTest {

    @Test
    void shouldDetectSensitiveKeys() {
        assertTrue(SensitiveAuditValueMasker.isSensitive("refreshToken"));
        assertTrue(SensitiveAuditValueMasker.isSensitive("secretKey"));
        assertFalse(SensitiveAuditValueMasker.isSensitive("username"));
    }

    @Test
    void shouldReturnEmptyMapWhenAttributesEmpty() {
        assertEquals(Map.of(), SensitiveAuditValueMasker.mask(null));
        assertEquals(Map.of(), SensitiveAuditValueMasker.mask(Map.of()));
    }
}
