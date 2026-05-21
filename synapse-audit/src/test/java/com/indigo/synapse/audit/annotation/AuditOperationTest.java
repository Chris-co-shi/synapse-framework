package com.indigo.synapse.audit.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditOperationTest {

    @Test
    void shouldExposeAuditMetadataAtRuntime() throws NoSuchMethodException {
        Method method = UserService.class.getDeclaredMethod("disableUser");

        AuditOperation operation = method.getAnnotation(AuditOperation.class);

        assertEquals("system:user:disable", operation.action());
        assertEquals("USER", operation.targetType());
    }

    private static final class UserService {

        @AuditOperation(action = "system:user:disable", targetType = "USER")
        void disableUser() {
        }
    }
}
