package com.indigo.synapse.audit.infrastructure.persistence.port;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.infrastructure.persistence.converter.AuditLogPersistenceConverter;
import com.indigo.synapse.audit.infrastructure.persistence.entity.AuditLogEntity;
import com.indigo.synapse.audit.port.AuditLogPort;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MybatisPlusAuditLogPortTest {

    @Test
    void shouldPersistConvertedAuditEntity() {
        AtomicReference<AuditLogEntity> captured = new AtomicReference<>();
        BaseMapper<AuditLogEntity> mapper = mapper(captured);
        AuditLogPort port = new MybatisPlusAuditLogPort(mapper, new AuditLogPersistenceConverter(new com.fasterxml.jackson.databind.ObjectMapper()));

        port.record(event());

        assertEquals("system:user:create", captured.get().getAction());
    }

    @Test
    void shouldRejectNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new MybatisPlusAuditLogPort(null, new AuditLogPersistenceConverter(new com.fasterxml.jackson.databind.ObjectMapper())));
        assertThrows(IllegalArgumentException.class, () -> new MybatisPlusAuditLogPort(mapper(new AtomicReference<>()), null));
    }

    @SuppressWarnings("unchecked")
    private static BaseMapper<AuditLogEntity> mapper(AtomicReference<AuditLogEntity> captured) {
        return (BaseMapper<AuditLogEntity>) Proxy.newProxyInstance(
                MybatisPlusAuditLogPortTest.class.getClassLoader(),
                new Class[]{BaseMapper.class},
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        captured.set((AuditLogEntity) args[0]);
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static AuditEvent event() {
        return new AuditEvent(
                "system:user:create",
                new AuditSubject("USER", "1", "tenant-a"),
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                "trace-1",
                "created user",
                Map.of("username", "alice")
        );
    }
}
