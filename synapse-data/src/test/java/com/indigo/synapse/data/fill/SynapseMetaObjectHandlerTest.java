package com.indigo.synapse.data.fill;

import com.indigo.synapse.data.entity.BaseEntity;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SynapseMetaObjectHandlerTest {

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

    private static final class TestEntity extends BaseEntity {
    }
}
