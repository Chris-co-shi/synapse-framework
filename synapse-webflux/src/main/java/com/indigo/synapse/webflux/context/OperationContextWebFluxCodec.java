package com.indigo.synapse.webflux.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationSource;
import org.springframework.http.HttpHeaders;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * ServerWebExchange Header 到 OperationContext 的轻量解码器。
 *
 * <p>该解码器只恢复技术上下文，不做 Header 签名校验、认证、授权或 Gateway 业务判定。可信 Header
 * 的注入和校验应由 Platform Gateway 或后续安全适配完成。</p>
 */
public final class OperationContextWebFluxCodec {

    public Optional<OperationContextSnapshot> decode(
            HttpHeaders headers,
            String traceId,
            String requestId,
            String method,
            String path
    ) {
        if (headers == null) {
            return Optional.empty();
        }
        String actorId = trim(headers.getFirst(OperationContextHeaders.ACTOR_ID));
        if (actorId == null) {
            return Optional.empty();
        }
        String tenantId = trim(headers.getFirst(OperationContextHeaders.TENANT_ID));
        OperationActor actor = new OperationActor(
                actorType(headers.getFirst(OperationContextHeaders.ACTOR_TYPE)),
                actorId,
                fallback(headers.getFirst(OperationContextHeaders.ACTOR_NAME), actorId),
                tenantId,
                Map.of()
        );
        OperationActor initiator = initiator(headers, actor, tenantId);
        OperationSource source = new OperationSource(
                "HTTP",
                fallback(headers.getFirst(OperationContextHeaders.SOURCE_NAME), "webflux"),
                trim(headers.getFirst(OperationContextHeaders.SOURCE_INSTANCE_ID)),
                method + " " + path,
                Map.of()
        );
        OperationContext context = new OperationContext(
                actor,
                initiator,
                source,
                traceId,
                tenantId,
                requestId,
                Instant.now(),
                Map.of()
        );
        return Optional.of(new OperationContextSnapshot(context));
    }

    private static OperationActor initiator(HttpHeaders headers, OperationActor actor, String tenantId) {
        String initiatorId = trim(headers.getFirst(OperationContextHeaders.INITIATOR_ID));
        if (initiatorId == null) {
            return actor;
        }
        return new OperationActor(
                actorType(headers.getFirst(OperationContextHeaders.INITIATOR_TYPE)),
                initiatorId,
                fallback(headers.getFirst(OperationContextHeaders.INITIATOR_NAME), initiatorId),
                tenantId,
                Map.of()
        );
    }

    private static OperationActorType actorType(String value) {
        String type = trim(value);
        if (type == null) {
            return OperationActorType.USER;
        }
        try {
            return OperationActorType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return OperationActorType.USER;
        }
    }

    private static String fallback(String value, String fallback) {
        String trimmed = trim(value);
        return trimmed == null ? fallback : trimmed;
    }

    private static String trim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
