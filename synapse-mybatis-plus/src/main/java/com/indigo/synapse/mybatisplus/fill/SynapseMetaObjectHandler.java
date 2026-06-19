package com.indigo.synapse.mybatisplus.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.indigo.synapse.data.audit.DataAuditorProvider;
import com.indigo.synapse.data.field.DataFieldNames;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Clock;
import java.time.Instant;

public class SynapseMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;
    private final DataAuditorProvider auditorProvider;

    public SynapseMetaObjectHandler(Clock clock) {
        this(clock, DataAuditorProvider.empty());
    }

    public SynapseMetaObjectHandler(Clock clock, DataAuditorProvider auditorProvider) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.auditorProvider = auditorProvider == null ? DataAuditorProvider.empty() : auditorProvider;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now(clock);
        fillIfNull(metaObject, DataFieldNames.CREATED_AT, now);
        fillIfNull(metaObject, DataFieldNames.UPDATED_AT, now);
        fillIfNull(metaObject, DataFieldNames.DELETED, 0);
        fillIfNull(metaObject, DataFieldNames.VERSION, 0);
        auditorProvider.currentAuditor().ifPresent(auditor -> {
            fillIfNull(metaObject, DataFieldNames.CREATED_BY, auditor);
            fillIfNull(metaObject, DataFieldNames.UPDATED_BY, auditor);
        });
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fill(metaObject, DataFieldNames.UPDATED_AT, Instant.now(clock));
        auditorProvider.currentAuditor()
                .ifPresent(auditor -> fill(metaObject, DataFieldNames.UPDATED_BY, auditor));
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
