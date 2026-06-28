package com.indigo.synapse.audit.event;

import com.indigo.synapse.audit.context.AuditContext;
import com.indigo.synapse.audit.context.AuditContextSnapshot;
import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.core.context.OperationSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 为审计事件补齐当前操作上下文。
 *
 * <p>补齐来源优先级为显式事件字段、AuditContext、OperationContextProvider。
 * 该组件不伪造 system/unknown 用户，也不覆盖调用方已经写入的 attributes。</p>
 */
public final class AuditEventContextEnricher {

    private final OperationContextProvider operationContextProvider;

    public AuditEventContextEnricher(OperationContextProvider operationContextProvider) {
        if (operationContextProvider == null) {
            throw new IllegalArgumentException("operationContextProvider must not be null");
        }
        this.operationContextProvider = operationContextProvider;
    }

    public AuditEvent enrich(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        AuditContextSnapshot auditContext = AuditContext.current().orElse(null);
        OperationContext operationContext = operationContextProvider.current().orElse(null);

        AuditSubject subject = resolveSubject(event, auditContext, operationContext);
        String traceId = resolveTraceId(event, auditContext, operationContext);
        Map<String, String> attributes = enrichAttributes(event.attributes(), operationContext);

        return new AuditEvent(
                event.action(),
                subject,
                event.target(),
                event.occurredAt(),
                event.outcome(),
                traceId,
                event.message(),
                attributes
        );
    }

    private AuditSubject resolveSubject(
            AuditEvent event,
            AuditContextSnapshot auditContext,
            OperationContext operationContext
    ) {
        if (event.subject() != null) {
            return event.subject();
        }
        if (auditContext != null && auditContext.subject() != null) {
            return auditContext.subject();
        }
        return operationContext == null ? null : toSubject(operationContext).orElse(null);
    }

    private Optional<AuditSubject> toSubject(OperationContext context) {
        OperationActor actor = context.actor();
        if (actor == null || actor.id() == null || actor.id().isBlank()) {
            return Optional.empty();
        }
        if (actor.type() == OperationActorType.SYSTEM || actor.type() == OperationActorType.UNKNOWN) {
            return Optional.empty();
        }
        return Optional.of(new AuditSubject(actor.type().name(), actor.id(), context.tenantId()));
    }

    private String resolveTraceId(
            AuditEvent event,
            AuditContextSnapshot auditContext,
            OperationContext operationContext
    ) {
        if (event.traceId() != null && !event.traceId().isBlank()) {
            return event.traceId();
        }
        if (auditContext != null && auditContext.traceId() != null) {
            return auditContext.traceId();
        }
        if (operationContext != null && operationContext.traceId() != null && !operationContext.traceId().isBlank()) {
            return operationContext.traceId();
        }
        return event.traceId();
    }

    private Map<String, String> enrichAttributes(
            Map<String, String> attributes,
            OperationContext operationContext
    ) {
        Map<String, String> enriched = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
        enriched.putIfAbsent("audit.eventId", UUID.randomUUID().toString());
        if (operationContext != null) {
            putActor(enriched, "operation.actor", operationContext.actor());
            putActor(enriched, "operation.initiator", operationContext.initiator());
            putIfPresent(enriched, "operation.requestId", operationContext.requestId());
            putSource(enriched, operationContext.source());
            if (operationContext.source() != null) {
                putIfPresent(enriched, "audit.sourceService", operationContext.source().name());
            }
        }
        return enriched;
    }

    private void putActor(Map<String, String> attributes, String prefix, OperationActor actor) {
        if (actor == null) {
            return;
        }
        if (actor.type() == OperationActorType.SYSTEM || actor.type() == OperationActorType.UNKNOWN) {
            return;
        }
        putIfPresent(attributes, prefix + ".type", actor.type() == null ? null : actor.type().name());
        putIfPresent(attributes, prefix + ".id", actor.id());
        putIfPresent(attributes, prefix + ".name", actor.name());
    }

    private void putSource(Map<String, String> attributes, OperationSource source) {
        if (source == null) {
            return;
        }
        putIfPresent(attributes, "operation.source.type", source.type());
        putIfPresent(attributes, "operation.source.name", source.name());
        putIfPresent(attributes, "operation.source.instanceId", source.instanceId());
        putIfPresent(attributes, "operation.source.entrypoint", source.entrypoint());
    }

    private void putIfPresent(Map<String, String> attributes, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        attributes.putIfAbsent(key, value);
    }
}
