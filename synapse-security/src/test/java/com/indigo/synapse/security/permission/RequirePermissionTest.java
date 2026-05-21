package com.indigo.synapse.security.permission;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequirePermissionTest {

    @Test
    void shouldExposePermissionValueAtRuntime() throws NoSuchMethodException {
        Method method = SecuredService.class.getDeclaredMethod("createUser");

        RequirePermission permission = method.getAnnotation(RequirePermission.class);

        assertEquals("system:user:create", permission.value());
    }

    private static final class SecuredService {

        @RequirePermission("system:user:create")
        void createUser() {
        }
    }
}
