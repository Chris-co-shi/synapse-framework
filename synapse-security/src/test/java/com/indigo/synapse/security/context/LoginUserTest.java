package com.indigo.synapse.security.context;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUserTest {

    @Test
    void shouldExposeImmutablePermissions() {
        LoginUser user = new LoginUser(
                "1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("system:user:list")
        );

        assertTrue(user.hasPermission("system:user:list"));
        assertFalse(user.hasPermission("system:user:create"));
        assertThrows(UnsupportedOperationException.class, () -> user.permissions().add("x"));
    }

    @Test
    void shouldRejectInvalidLoginUser() {
        assertThrows(IllegalArgumentException.class, () -> new LoginUser("", "admin", null, Set.of(), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new LoginUser("1", "", null, Set.of(), Set.of()));
    }
}
