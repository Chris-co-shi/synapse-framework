package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.indigo.synapse.data.field.DataFieldNames;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class EntityBaseContractTest {

    @Test
    void shouldExposeExpectedInheritanceHierarchy() {
        assertThat(CreatedEntity.class.getSuperclass()).isEqualTo(IdEntity.class);
        assertThat(MutableEntity.class.getSuperclass()).isEqualTo(CreatedEntity.class);
        assertThat(VersionedEntity.class.getSuperclass()).isEqualTo(MutableEntity.class);
        assertThat(ManagedEntity.class.getSuperclass()).isEqualTo(VersionedEntity.class);
    }

    @Test
    void shouldUseStringAssignId() throws NoSuchFieldException {
        var field = IdEntity.class.getDeclaredField(DataFieldNames.ID);
        var tableId = field.getAnnotation(TableId.class);

        assertThat(field.getType()).isEqualTo(String.class);
        assertThat(tableId).isNotNull();
        assertThat(tableId.type()).isEqualTo(IdType.ASSIGN_ID);
    }

    @Test
    void shouldDeclareAuditFillRules() throws NoSuchFieldException {
        assertFill(CreatedEntity.class, DataFieldNames.CREATED_AT, FieldFill.INSERT);
        assertFill(CreatedEntity.class, DataFieldNames.CREATED_BY, FieldFill.INSERT);
        assertFill(MutableEntity.class, DataFieldNames.UPDATED_AT, FieldFill.INSERT_UPDATE);
        assertFill(MutableEntity.class, DataFieldNames.UPDATED_BY, FieldFill.INSERT_UPDATE);
    }

    @Test
    void shouldDeclareRevisionAsOptimisticLockField() throws NoSuchFieldException {
        var field = VersionedEntity.class.getDeclaredField(DataFieldNames.REVISION);

        assertThat(field.getType()).isEqualTo(Integer.class);
        assertThat(field.getAnnotation(Version.class)).isNotNull();
        assertThat(DataFieldNames.REVISION).isEqualTo("revision");
        assertThat(DataFieldNames.class.getDeclaredField("VERSION").getAnnotation(Deprecated.class)).isNotNull();
    }

    @Test
    void shouldDeclareManagedDeletionRule() throws NoSuchFieldException {
        var field = ManagedEntity.class.getDeclaredField(DataFieldNames.DELETED);
        var tableLogic = field.getAnnotation(TableLogic.class);

        assertThat(field.getType()).isEqualTo(Integer.class);
        assertThat(tableLogic).isNotNull();
        assertThat(tableLogic.value()).isEqualTo("0");
        assertThat(tableLogic.delval()).isEqualTo("1");
    }

    @Test
    void shouldNotGenerateCustomEqualityInFrameworkBaseClasses() {
        assertNoDeclaredEquality(IdEntity.class);
        assertNoDeclaredEquality(CreatedEntity.class);
        assertNoDeclaredEquality(MutableEntity.class);
        assertNoDeclaredEquality(VersionedEntity.class);
        assertNoDeclaredEquality(ManagedEntity.class);
    }

    private static void assertFill(Class<?> type, String fieldName, FieldFill expected) throws NoSuchFieldException {
        var tableField = type.getDeclaredField(fieldName).getAnnotation(TableField.class);

        assertThat(tableField).isNotNull();
        assertThat(tableField.fill()).isEqualTo(expected);
    }

    private static void assertNoDeclaredEquality(Class<?> type) {
        var methodNames = Arrays.stream(type.getDeclaredMethods())
                .map(Method::getName)
                .toList();

        assertThat(methodNames).doesNotContain("equals", "hashCode");
    }
}
