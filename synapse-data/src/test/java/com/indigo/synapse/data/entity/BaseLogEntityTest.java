package com.indigo.synapse.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BaseLogEntityTest {

    @Test
    void shouldKeepStandardLogFields() {
        Instant now = Instant.parse("2026-05-20T10:00:00Z");
        BaseLogEntity entity = new TestLogEntity();

        entity.setId("1");
        entity.setTenantId("2");
        entity.setCreatedAt(now);
        entity.setCreatedBy("3");

        assertEquals("1", entity.getId());
        assertEquals("2", entity.getTenantId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals("3", entity.getCreatedBy());
    }

    @Test
    void shouldDeclareMybatisPlusAnnotations() throws NoSuchFieldException {
        Field id = BaseLogEntity.class.getDeclaredField("id");
        TableId tableId = id.getAnnotation(TableId.class);
        assertNotNull(tableId);
        assertEquals("id", tableId.value());
        assertEquals(IdType.ASSIGN_ID, tableId.type());

        assertTableField("tenantId", "tenant_id", FieldFill.DEFAULT);
        assertTableField("createdAt", "created_at", FieldFill.INSERT);
        assertTableField("createdBy", "created_by", FieldFill.INSERT);
    }

    private static void assertTableField(String fieldName, String columnName, FieldFill fieldFill) throws NoSuchFieldException {
        TableField tableField = BaseLogEntity.class.getDeclaredField(fieldName).getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(columnName, tableField.value());
        assertEquals(fieldFill, tableField.fill());
    }

    private static final class TestLogEntity extends BaseLogEntity {
    }
}
