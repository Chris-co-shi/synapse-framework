package com.indigo.synapse.security.context;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedUserTest {

    @Test
    void shouldExposeImmutablePermissions() {
        AuthenticatedUser user = new AuthenticatedUser(
                "1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("system:user:list")
        );

        assertTrue(user.hasPermission("system:user:list"));
        assertFalse(user.hasPermission("system:user:create"));
        assertThrows(UnsupportedOperationException.class, () -> user.roles().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> user.permissions().add("x"));
    }

    @Test
    void shouldRejectInvalidAuthenticatedUser() {
        assertThrows(IllegalArgumentException.class, () -> new AuthenticatedUser("", "admin", null, Set.of(), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new AuthenticatedUser("1", "", null, Set.of(), Set.of()));
    }
}
