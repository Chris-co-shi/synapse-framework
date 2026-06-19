package com.indigo.synapse.mybatisplus.fill;

import com.indigo.synapse.data.audit.DataAuditorProvider;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMetaObjectHandlerTest {

    @Test
    void shouldFillAuditFieldsButNotDeletedAndVersion() {
        TestEntity entity = new TestEntity();
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                () -> Optional.of("alice")
        );

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertThat(entity.createdAt).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(entity.updatedAt).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(entity.createdBy).isEqualTo("alice");
        assertThat(entity.updatedBy).isEqualTo("alice");
        assertThat(entity.deleted).isNull();
        assertThat(entity.version).isNull();
    }

    @Test
    void shouldSkipAuditorWhenProviderIsEmpty() {
        TestEntity entity = new TestEntity();
        SynapseMetaObjectHandler handler = new SynapseMetaObjectHandler(
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                DataAuditorProvider.empty()
        );

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertThat(entity.createdBy).isNull();
        assertThat(entity.updatedBy).isNull();
    }

    static class TestEntity {
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        private Boolean deleted;
        private Long version;

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public Boolean getDeleted() {
            return deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }

        public Long getVersion() {
            return version;
        }

        public void setVersion(Long version) {
            this.version = version;
        }
    }
}
