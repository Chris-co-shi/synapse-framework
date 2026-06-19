package com.indigo.synapse.mybatisplus.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.indigo.synapse.data.audit.DataAuditorProvider;
import com.indigo.synapse.data.field.DataFieldNames;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Clock;
import java.time.Instant;

/**
 * Synapse MyBatis-Plus 自动字段填充处理器。
 *
 * <p>该处理器只负责填充 ORM 无关字段约定中的创建/更新时间和创建/更新人，不负责初始化
 * 逻辑删除字段和乐观锁字段，避免业务实体使用 Boolean、Long 等不同字段类型时出现类型不匹配。</p>
 *
 * <p>该类型属于 `synapse-mybatis-plus` 模块，只依赖 MyBatis-Plus 的 {@link MetaObjectHandler}
 * 扩展点，不扫描业务 Mapper，不创建业务 Entity，也不处理 DataSource 治理。</p>
 *
 * <p>实例本身是线程安全的：内部只持有不可变的 {@link Clock} 和 {@link DataAuditorProvider}
 * 引用；填充时只修改当前 MyBatis-Plus 传入的单个实体 MetaObject。</p>
 */
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
