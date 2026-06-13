package com.indigo.synapse.tenant.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldStoreAndClearTenantContext() {
        TenantContextSnapshot snapshot = new TenantContextSnapshot("tenant-a");

        TenantContext.set(snapshot);

        assertEquals(snapshot, TenantContext.current().orElseThrow());
        assertEquals("tenant-a", TenantContext.currentTenantId().orElseThrow());

        TenantContext.clear();

        assertTrue(TenantContext.current().isEmpty());
    }

    @Test
    void shouldSupportScopedTenantContext() {
        TenantContext.setTenantId("tenant-a");

        try (TenantContextScope ignored = TenantContext.scope(new TenantContextSnapshot("tenant-b"))) {
            assertEquals("tenant-b", TenantContext.currentTenantId().orElseThrow());
        }

        assertEquals("tenant-a", TenantContext.currentTenantId().orElseThrow());
    }

    @Test
    void shouldClearWhenSetBlankTenantId() {
        TenantContext.setTenantId("tenant-a");
        TenantContext.setTenantId(" ");

        assertTrue(TenantContext.currentTenantId().isEmpty());
    }

    @Test
    void shouldRejectInvalidSnapshot() {
        assertThrows(IllegalArgumentException.class, () -> new TenantContextSnapshot(""));
    }
}
