package com.indigo.synapse.audit.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogPersistenceConverter {

    private final ObjectMapper objectMapper;

    public AuditLogPersistenceConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuditLogEntity toEntity(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAction(event.action());
        entity.setSubjectType(event.subject().subjectType());
        entity.setSubjectId(event.subject().subjectId());
        entity.setSubjectTenantId(event.subject().tenantId());
        entity.setTargetType(event.target().targetType());
        entity.setTargetId(event.target().targetId());
        entity.setOutcome(event.outcome().name());
        entity.setTraceId(event.traceId());
        entity.setMessage(event.message());
        entity.setAttributesJson(writeAttributes(event));
        return entity;
    }

    private String writeAttributes(AuditEvent event) {
        try {
            return objectMapper.writeValueAsString(event.attributes());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize audit event attributes", exception);
        }
    }
}
