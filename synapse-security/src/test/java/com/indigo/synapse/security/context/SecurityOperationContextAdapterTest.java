package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityOperationContextAdapterTest {

    @Test
    void shouldMapLoginUserToOperationActor() {
        LoginUser loginUser = loginUser();

        OperationActor actor = SecurityOperationContextAdapter.toOperationActor(loginUser);

        assertEquals(OperationActorType.USER, actor.type());
        assertEquals("user-1", actor.id());
        assertEquals("admin", actor.name());
        assertEquals("tenant-a", actor.tenantId());
        assertTrue(actor.attributes().isEmpty());
    }

    @Test
    void shouldMapLoginUserToOperationContextWithoutSecurityModel() {
        LoginUser loginUser = loginUser();

        OperationContext context = SecurityOperationContextAdapter.toOperationContext(loginUser);

        assertEquals(context.actor(), context.initiator());
        assertEquals("tenant-a", context.tenantId());
        assertNull(context.traceId());
        assertNull(context.requestId());
        assertNull(context.source());
        assertTrue(context.attributes().isEmpty());
        assertTrue(context.actor().attributes().isEmpty());
        assertTrue(context.occurredAt() != null);
    }

    @Test
    void shouldRejectNullLoginUser() {
        assertThrows(IllegalArgumentException.class, () -> SecurityOperationContextAdapter.toOperationActor(null));
        assertThrows(IllegalArgumentException.class, () -> SecurityOperationContextAdapter.toOperationContext(null));
    }

    private static LoginUser loginUser() {
        return new LoginUser(
                "user-1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("system:user:list")
        );
    }
}
