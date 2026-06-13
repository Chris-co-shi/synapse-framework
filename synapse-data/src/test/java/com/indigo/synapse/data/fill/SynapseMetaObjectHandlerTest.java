package com.indigo.synapse.data.fill;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.data.entity.BaseEntity;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SynapseMetaObjectHandlerTest {

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void shouldFillInsertAuditFields() {
        Instant now = Instant.parse("2026-05-21T01:02:03Z");
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                () -> Optional.of("1001")
        );
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals("1001", entity.getCreatedBy());
        assertEquals("1001", entity.getUpdatedBy());
        assertEquals(0, entity.getDeleted());
        assertEquals(0, entity.getVersion());
    }

    @Test
    void shouldFillUpdateAuditFields() {
        Instant now = Instant.parse("2026-05-21T01:02:03Z");
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                () -> Optional.of("1001")
        );
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.updateFill(metaObject);

        assertEquals(now, entity.getUpdatedAt());
        assertEquals("1001", entity.getUpdatedBy());
    }

    @Test
    void shouldFillInsertAuditFieldsFromOperationContext() {
        Instant now = Instant.parse("2026-05-21T01:02:03Z");
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                SynapseAuditorProvider.from(new DefaultOperationContextProvider())
        );
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        try (OperationContextScope ignored = OperationContextHolder.scope(operationContext("user-1", "tenant-a"))) {
            handler.insertFill(metaObject);
        }

        assertEquals("user-1", entity.getCreatedBy());
        assertEquals("user-1", entity.getUpdatedBy());
        assertEquals("tenant-a", entity.getTenantId());
    }

    @Test
    void shouldFillUpdateAuditFieldsFromOperationContext() {
        Instant now = Instant.parse("2026-05-21T01:02:03Z");
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                SynapseAuditorProvider.from(new DefaultOperationContextProvider())
        );
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        try (OperationContextScope ignored = OperationContextHolder.scope(operationContext("user-2", "tenant-a"))) {
            handler.updateFill(metaObject);
        }

        assertEquals(now, entity.getUpdatedAt());
        assertEquals("user-2", entity.getUpdatedBy());
        assertNull(entity.getTenantId());
    }

    @Test
    void shouldNotFillAuditorOrTenantWhenOperationContextMissing() {
        Instant now = Instant.parse("2026-05-21T01:02:03Z");
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                SynapseAuditorProvider.from(new DefaultOperationContextProvider())
        );
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertNull(entity.getCreatedBy());
        assertNull(entity.getUpdatedBy());
        assertNull(entity.getTenantId());
    }

    @Test
    void shouldNotOverrideExistingInsertFields() {
        Instant now = Instant.parse("2026-05-21T01:02:03Z");
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                SynapseAuditorProvider.from(new DefaultOperationContextProvider())
        );
        TestEntity entity = new TestEntity();
        entity.setCreatedBy("manual-created");
        entity.setUpdatedBy("manual-updated");
        entity.setTenantId("manual-tenant");
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        try (OperationContextScope ignored = OperationContextHolder.scope(operationContext("user-3", "tenant-a"))) {
            handler.insertFill(metaObject);
        }

        assertEquals("manual-created", entity.getCreatedBy());
        assertEquals("manual-updated", entity.getUpdatedBy());
        assertEquals("manual-tenant", entity.getTenantId());
    }

    private static final class TestEntity extends BaseEntity {
    }

    private static OperationContext operationContext(String actorId, String tenantId) {
        OperationActor actor = new OperationActor(OperationActorType.USER, actorId, actorId, tenantId, Map.of());
        return new OperationContext(
                actor,
                actor,
                null,
                "trace-1",
                tenantId,
                "request-1",
                Instant.parse("2026-05-21T00:00:00Z"),
                Map.of()
        );
    }
}
