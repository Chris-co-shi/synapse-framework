package com.indigo.synapse.example.foundation;

import com.indigo.synapse.audit.event.AuditEvent;

public record ExampleResult(
        boolean webEnabled,
        boolean dataEnabled,
        boolean cacheEnabled,
        boolean securityEnabled,
        boolean auditEnabled,
        boolean createdExternalCacheConnection,
        String responseCode,
        String cacheKey,
        ExampleDataUsage dataUsage,
        ExampleCacheUsage cacheUsage,
        ExampleSecurityUsage securityUsage,
        AuditEvent auditEvent
) {
}
