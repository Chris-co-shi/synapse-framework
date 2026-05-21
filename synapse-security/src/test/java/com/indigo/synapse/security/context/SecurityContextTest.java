package com.indigo.synapse.security.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityContextTest {

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    void shouldStoreAndClearCurrentUser() {
        LoginUser user = new LoginUser("1", "admin", null, Set.of(), Set.of());

        SecurityContext.set(user);

        assertEquals(user, SecurityContext.currentUser().orElseThrow());

        SecurityContext.clear();

        assertTrue(SecurityContext.currentUser().isEmpty());
    }

    @Test
    void shouldClearWhenSetNull() {
        SecurityContext.set(new LoginUser("1", "admin", null, Set.of(), Set.of()));

        SecurityContext.set(null);

        assertTrue(SecurityContext.currentUser().isEmpty());
    }
}
