package com.indigo.synapse.cloud.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperationContextHttpHeaderCodecTest {

    private final OperationContextHttpHeaderCodec codec = new OperationContextHttpHeaderCodec();

    @Test
    void shouldEncodeOperationContextToHeaders() {
        Map<String, String> headers = codec.encode(context());

        assertThat(headers)
                .containsEntry(SynapseCloudHeaders.TRACE_ID, "trace-1")
                .containsEntry(SynapseCloudHeaders.REQUEST_ID, "request-1")
                .containsEntry(SynapseCloudHeaders.TENANT_ID, "tenant-1")
                .containsEntry(SynapseCloudHeaders.ACTOR_TYPE, "USER")
                .containsEntry(SynapseCloudHeaders.ACTOR_ID, "user-1")
                .containsEntry(SynapseCloudHeaders.ACTOR_NAME, "Alice")
                .containsEntry(SynapseCloudHeaders.INITIATOR_TYPE, "SERVICE")
                .containsEntry(SynapseCloudHeaders.INITIATOR_ID, "gateway")
                .containsEntry(SynapseCloudHeaders.INITIATOR_NAME, "Gateway")
                .containsEntry(SynapseCloudHeaders.SOURCE_TYPE, "HTTP")
                .containsEntry(SynapseCloudHeaders.SOURCE_NAME, "order-service")
                .containsEntry(SynapseCloudHeaders.LOCALE, "zh-CN")
                .containsEntry(SynapseCloudHeaders.TIME_ZONE, "Asia/Shanghai");
    }

    @Test
    void shouldNotWriteActorHeadersWhenActorIsMissing() {
        OperationContext context = new OperationContext(
                null,
                null,
                null,
                "trace-1",
                "tenant-1",
                "request-1",
                Instant.now(),
                Map.of()
        );

        Map<String, String> headers = codec.encode(context);

        assertThat(headers)
                .containsEntry(SynapseCloudHeaders.TRACE_ID, "trace-1")
                .doesNotContainKey(SynapseCloudHeaders.ACTOR_TYPE)
                .doesNotContainKey(SynapseCloudHeaders.ACTOR_ID)
                .doesNotContainKey(SynapseCloudHeaders.ACTOR_NAME);
    }

    @Test
    void shouldSkipNullOrBlankHeaders() {
        OperationContext context = new OperationContext(
                new OperationActor(OperationActorType.USER, " ", null, null, Map.of()),
                null,
                null,
                " ",
                null,
                "",
                Instant.now(),
                Map.of(SynapseCloudHeaders.ATTRIBUTE_LOCALE, " ")
        );

        Map<String, String> headers = codec.encode(context);

        assertThat(headers)
                .containsEntry(SynapseCloudHeaders.ACTOR_TYPE, "USER")
                .doesNotContainKey(SynapseCloudHeaders.ACTOR_ID)
                .doesNotContainKey(SynapseCloudHeaders.TRACE_ID)
                .doesNotContainKey(SynapseCloudHeaders.REQUEST_ID)
                .doesNotContainKey(SynapseCloudHeaders.LOCALE);
    }

    @Test
    void shouldNotOverrideExistingHeadersByDefault() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(SynapseCloudHeaders.TRACE_ID, "existing");

        codec.write(context(), headers::put, headers::containsKey, false);

        assertThat(headers.get(SynapseCloudHeaders.TRACE_ID)).isEqualTo("existing");
        assertThat(headers.get(SynapseCloudHeaders.REQUEST_ID)).isEqualTo("request-1");
    }

    @Test
    void shouldOverrideExistingHeadersWhenAllowed() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(SynapseCloudHeaders.TRACE_ID, "existing");

        codec.write(context(), headers::put, headers::containsKey, true);

        assertThat(headers.get(SynapseCloudHeaders.TRACE_ID)).isEqualTo("trace-1");
    }

    @Test
    void shouldDecodeHeadersToSnapshotWhenActorIsComplete() {
        Map<String, String> headers = codec.encode(context());

        OperationContextSnapshot snapshot = codec.decode(headers).orElseThrow();

        assertThat(snapshot.context().actor().id()).isEqualTo("user-1");
        assertThat(snapshot.context().initiator().id()).isEqualTo("gateway");
        assertThat(snapshot.context().source().name()).isEqualTo("order-service");
        assertThat(snapshot.context().traceId()).isEqualTo("trace-1");
    }

    @Test
    void shouldNotDecodeWhenActorIsMissing() {
        Map<String, String> headers = Map.of(SynapseCloudHeaders.TRACE_ID, "trace-1");

        assertThat(codec.decode(headers)).isEmpty();
    }

    private OperationContext context() {
        OperationActor actor = new OperationActor(OperationActorType.USER, "user-1", "Alice", "tenant-1", Map.of());
        OperationActor initiator = new OperationActor(OperationActorType.SERVICE, "gateway", "Gateway", "tenant-1", Map.of());
        OperationSource source = new OperationSource("HTTP", "order-service", "instance-1", "GET /orders", Map.of());
        return new OperationContext(
                actor,
                initiator,
                source,
                "trace-1",
                "tenant-1",
                "request-1",
                Instant.now(),
                Map.of(
                        SynapseCloudHeaders.ATTRIBUTE_LOCALE, "zh-CN",
                        SynapseCloudHeaders.ATTRIBUTE_TIME_ZONE, "Asia/Shanghai"
                )
        );
    }
}
