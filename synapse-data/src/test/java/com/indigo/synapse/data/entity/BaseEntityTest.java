package com.indigo.synapse.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BaseEntityTest {

    @Test
    void shouldKeepStandardBusinessFields() {
        Instant now = Instant.parse("2026-05-20T10:00:00Z");
        BaseEntity entity = new TestEntity();

        entity.setId("1");
        entity.setTenantId("2");
        entity.setCreatedAt(now);
        entity.setCreatedBy("3");
        entity.setUpdatedAt(now);
        entity.setUpdatedBy("4");
        entity.setDeleted(0);
        entity.setVersion(5);

        assertEquals("1", entity.getId());
        assertEquals("2", entity.getTenantId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals("3", entity.getCreatedBy());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals("4", entity.getUpdatedBy());
        assertEquals(0, entity.getDeleted());
        assertEquals(5, entity.getVersion());
    }

    @Test
    void shouldDeclareMybatisPlusAnnotations() throws NoSuchFieldException {
        Field id = BaseEntity.class.getDeclaredField("id");
        TableId tableId = id.getAnnotation(TableId.class);
        assertNotNull(tableId);
        assertEquals("id", tableId.value());
        assertEquals(IdType.ASSIGN_ID, tableId.type());

        assertTableField("tenantId", "tenant_id", FieldFill.DEFAULT);
        assertTableField("createdAt", "created_at", FieldFill.INSERT);
        assertTableField("createdBy", "created_by", FieldFill.INSERT);
        assertTableField("updatedAt", "updated_at", FieldFill.INSERT_UPDATE);
        assertTableField("updatedBy", "updated_by", FieldFill.INSERT_UPDATE);
        assertTableField("deleted", "deleted", FieldFill.DEFAULT);
        assertTableField("version", "version", FieldFill.DEFAULT);

        assertNotNull(BaseEntity.class.getDeclaredField("deleted").getAnnotation(TableLogic.class));
        assertNotNull(BaseEntity.class.getDeclaredField("version").getAnnotation(Version.class));
    }

    private static void assertTableField(String fieldName, String columnName, FieldFill fieldFill) throws NoSuchFieldException {
        TableField tableField = BaseEntity.class.getDeclaredField(fieldName).getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(columnName, tableField.value());
        assertEquals(fieldFill, tableField.fill());
    }

    private static final class TestEntity extends BaseEntity {
    }
}
