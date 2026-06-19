package com.indigo.synapse.webmvc.page;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortWhitelistTest {

    private final SortWhitelist whitelist = SortWhitelist.of(Map.of(
            "createdAt", "created_at",
            "username", "username"
    ));

    @Test
    void shouldResolveAllowedSortField() {
        Optional<SortOrder> order = whitelist.resolve("createdAt", "desc");

        assertTrue(order.isPresent());
        assertEquals("created_at", order.get().getProperty());
        assertEquals(SortDirection.DESC, order.get().getDirection());
    }

    @Test
    void shouldIgnoreUnknownSortField() {
        Optional<SortOrder> order = whitelist.resolve("deleted;drop table", "asc");

        assertTrue(order.isEmpty());
    }

    @Test
    void shouldUseAscByDefault() {
        Optional<SortOrder> order = whitelist.resolve("username", null);

        assertTrue(order.isPresent());
        assertEquals(SortDirection.ASC, order.get().getDirection());
    }
}
