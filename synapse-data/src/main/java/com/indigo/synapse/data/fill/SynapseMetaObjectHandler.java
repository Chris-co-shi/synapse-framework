package com.indigo.synapse.data.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Clock;
import java.time.Instant;

public class SynapseMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;
    private final SynapseAuditorProvider auditorProvider;

    public SynapseMetaObjectHandler(Clock clock, SynapseAuditorProvider auditorProvider) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.auditorProvider = auditorProvider == null ? SynapseAuditorProvider.empty() : auditorProvider;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now(clock);
        fillIfNull(metaObject, "createdAt", now);
        fillIfNull(metaObject, "updatedAt", now);
        fillIfNull(metaObject, "deleted", 0);
        fillIfNull(metaObject, "version", 0);
        auditorProvider.currentAuditor().ifPresent(auditor -> {
            fillIfNull(metaObject, "createdBy", auditor);
            fillIfNull(metaObject, "updatedBy", auditor);
        });
        auditorProvider.currentTenantId()
                .ifPresent(tenantId -> fillIfNull(metaObject, "tenantId", tenantId));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fill(metaObject, "updatedAt", Instant.now(clock));
        auditorProvider.currentAuditor()
                .ifPresent(auditor -> fill(metaObject, "updatedBy", auditor));
    }

    private static void fillIfNull(MetaObject metaObject, String fieldName, Object value) {
        if (!metaObject.hasSetter(fieldName) || metaObject.getValue(fieldName) != null) {
            return;
        }
        metaObject.setValue(fieldName, value);
    }

    private static void fill(MetaObject metaObject, String fieldName, Object value) {
        if (!metaObject.hasSetter(fieldName)) {
            return;
        }
        metaObject.setValue(fieldName, value);
    }
}
