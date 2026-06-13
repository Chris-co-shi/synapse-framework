package com.indigo.synapse.audit.infrastructure.persistence.port;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.infrastructure.persistence.converter.AuditLogPersistenceConverter;
import com.indigo.synapse.audit.infrastructure.persistence.entity.AuditLogEntity;
import com.indigo.synapse.audit.port.AuditLogPort;

public class MybatisPlusAuditLogPort implements AuditLogPort {

    private final BaseMapper<AuditLogEntity> auditLogMapper;
    private final AuditLogPersistenceConverter converter;

    public MybatisPlusAuditLogPort(BaseMapper<AuditLogEntity> auditLogMapper, AuditLogPersistenceConverter converter) {
        if (auditLogMapper == null) {
            throw new IllegalArgumentException("auditLogMapper must not be null");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter must not be null");
        }
        this.auditLogMapper = auditLogMapper;
        this.converter = converter;
    }

    @Override
    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        auditLogMapper.insert(converter.toEntity(event));
    }
}
