package com.indigo.synapse.data.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Clock;
import java.time.Instant;

/**
 * MyBatis-Plus 自动字段填充处理器。
 *
 * <p>该处理器只按约定字段名填充通用技术字段，不依赖业务 Entity 基类。业务实体只要存在对应 setter，
 * 即可在插入或更新时被填充；不存在对应字段时会自动跳过。</p>
 *
 * <p>插入时只填充空值，避免覆盖业务侧显式设置的 createdAt、createdBy 等字段；更新时会刷新 updatedAt
 * 和 updatedBy。当前约定字段名包括 createdAt、updatedAt、createdBy、updatedBy、deleted、version、tenantId。</p>
 */
public class SynapseMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;
    private final SynapseAuditorProvider auditorProvider;

    public SynapseMetaObjectHandler(Clock clock, SynapseAuditorProvider auditorProvider) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.auditorProvider = auditorProvider == null ? SynapseAuditorProvider.empty() : auditorProvider;
    }

    /**
     * 插入填充。
     *
     * <p>只填充当前为空的字段，避免覆盖消费方显式赋值。</p>
     */
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

    /**
     * 更新填充。
     *
     * <p>更新时总是刷新 updatedAt；如果存在当前操作人，则刷新 updatedBy。</p>
     */
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
