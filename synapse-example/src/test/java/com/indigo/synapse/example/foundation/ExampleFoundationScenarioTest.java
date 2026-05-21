package com.indigo.synapse.example.foundation;

import com.indigo.synapse.audit.event.SensitiveAuditValueMasker;
import com.indigo.synapse.common.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExampleFoundationScenarioTest {

    @Test
    void shouldDemonstrateFoundationContractsThroughStarter() {
        ExampleResult result = new ExampleFoundationScenario().run();

        assertTrue(result.webEnabled());
        assertTrue(result.dataEnabled());
        assertTrue(result.cacheEnabled());
        assertTrue(result.securityEnabled());
        assertTrue(result.auditEnabled());
        assertFalse(result.createdExternalCacheConnection());
        assertEquals(CommonErrorCode.SUCCESS.code(), result.responseCode());
        assertEquals("synapse:example:lock:demo:tenant-a:order-1", result.cacheKey());
        assertEquals("reporting", result.dataUsage().selectedDataSource());
        assertEquals("primary", result.dataUsage().restoredDataSource());
        assertEquals("POSTGRESQL", result.dataUsage().databaseType());
        assertTrue(result.dataUsage().supportsJsonColumn());
        assertTrue(result.cacheUsage().lockAcquired());
        assertTrue(result.cacheUsage().lockReentered());
        assertTrue(result.cacheUsage().lockReleased());
        assertTrue(result.cacheUsage().rateLimitAllowed());
        assertEquals(4L, result.cacheUsage().rateLimitRemaining());
        assertEquals("1", result.securityUsage().subject());
        assertEquals("ACCESS_TOKEN", result.securityUsage().tokenType());
        assertEquals("example-key", result.securityUsage().keyId());
        assertFalse(result.securityUsage().tokenExpired());
        assertTrue(result.securityUsage().oauth2EndpointPublic());
        assertTrue(result.securityUsage().permissionMatched());
        assertNotNull(result.auditEvent());
        assertEquals(SensitiveAuditValueMasker.MASKED, result.auditEvent().attributes().get("accessToken"));
        assertEquals(result.cacheKey(), result.auditEvent().attributes().get("cacheName"));
    }
}
