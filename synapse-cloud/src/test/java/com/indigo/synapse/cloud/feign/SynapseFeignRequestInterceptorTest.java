package com.indigo.synapse.cloud.feign;

import com.indigo.synapse.cloud.autoconfigure.SynapseFeignProperties;
import com.indigo.synapse.cloud.context.OperationContextHttpHeaderCodec;
import com.indigo.synapse.cloud.context.SynapseCloudHeaders;
import com.indigo.synapse.cloud.security.InternalCallSigner;
import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.core.context.OperationSource;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseFeignRequestInterceptorTest {

    @Test
    void shouldWriteContextHeadersWhenOperationContextExists() {
        RequestTemplate template = template();

        interceptor(contextProvider(context()), properties(false, false), null).apply(template);

        assertThat(first(template, SynapseCloudHeaders.TRACE_ID)).isEqualTo("trace-1");
        assertThat(first(template, SynapseCloudHeaders.REQUEST_ID)).isEqualTo("request-1");
        assertThat(first(template, SynapseCloudHeaders.TENANT_ID)).isEqualTo("tenant-1");
        assertThat(first(template, SynapseCloudHeaders.ACTOR_ID)).isEqualTo("user-1");
        assertThat(first(template, SynapseCloudHeaders.INITIATOR_ID)).isEqualTo("gateway");
        assertThat(first(template, SynapseCloudHeaders.SOURCE_NAME)).isEqualTo("order-service");
    }

    @Test
    void shouldNotWriteIdentityHeadersWhenOperationContextIsMissing() {
        RequestTemplate template = template();

        interceptor(contextProvider(null), properties(false, false), null).apply(template);

        assertThat(template.headers()).doesNotContainKey(SynapseCloudHeaders.ACTOR_ID);
        assertThat(template.headers()).doesNotContainKey(SynapseCloudHeaders.INITIATOR_ID);
        assertThat(template.headers()).doesNotContainKey(SynapseCloudHeaders.TENANT_ID);
    }

    @Test
    void shouldNotOverrideExistingHeadersByDefault() {
        RequestTemplate template = template();
        template.header(SynapseCloudHeaders.TRACE_ID, "existing");

        interceptor(contextProvider(context()), properties(false, false), null).apply(template);

        assertThat(first(template, SynapseCloudHeaders.TRACE_ID)).isEqualTo("existing");
    }

    @Test
    void shouldOverrideExistingHeadersWhenConfigured() {
        RequestTemplate template = template();
        template.header(SynapseCloudHeaders.TRACE_ID, "existing");

        interceptor(contextProvider(context()), properties(true, false), null).apply(template);

        assertThat(first(template, SynapseCloudHeaders.TRACE_ID)).isEqualTo("trace-1");
    }

    @Test
    void shouldNotWriteSignatureWhenSignerIsNoop() {
        RequestTemplate template = template();

        interceptor(contextProvider(context()), properties(false, true), null).apply(template);

        assertThat(template.headers()).doesNotContainKey(SynapseCloudHeaders.SIGNATURE);
    }

    @Test
    void shouldAllowCustomSignerToWriteSignatureHeaders() {
        RequestTemplate template = template();
        InternalCallSigner signer = (request, writer, reader, override) -> {
            if (override || !reader.contains(SynapseCloudHeaders.TIMESTAMP)) {
                writer.write(SynapseCloudHeaders.TIMESTAMP, "1000");
            }
            if (override || !reader.contains(SynapseCloudHeaders.NONCE)) {
                writer.write(SynapseCloudHeaders.NONCE, "nonce-1");
            }
            if (override || !reader.contains(SynapseCloudHeaders.SIGNATURE)) {
                writer.write(SynapseCloudHeaders.SIGNATURE, "signature-1");
            }
        };

        interceptor(contextProvider(context()), properties(false, true), signer).apply(template);

        assertThat(first(template, SynapseCloudHeaders.TIMESTAMP)).isEqualTo("1000");
        assertThat(first(template, SynapseCloudHeaders.NONCE)).isEqualTo("nonce-1");
        assertThat(first(template, SynapseCloudHeaders.SIGNATURE)).isEqualTo("signature-1");
    }

    private SynapseFeignRequestInterceptor interceptor(
            OperationContextProvider provider,
            SynapseFeignProperties properties,
            InternalCallSigner signer
    ) {
        return new SynapseFeignRequestInterceptor(
                provider,
                new OperationContextHttpHeaderCodec(),
                properties,
                signer
        );
    }

    private SynapseFeignProperties properties(boolean override, boolean signatureEnabled) {
        SynapseFeignProperties properties = new SynapseFeignProperties();
        properties.setOverrideExistingHeaders(override);
        properties.setInternalSignatureEnabled(signatureEnabled);
        return properties;
    }

    private OperationContextProvider contextProvider(OperationContext context) {
        return () -> Optional.ofNullable(context);
    }

    private RequestTemplate template() {
        RequestTemplate template = new RequestTemplate();
        template.method("GET");
        template.uri("/remote");
        return template;
    }

    private String first(RequestTemplate template, String header) {
        Collection<String> values = template.headers().get(header);
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }

    private OperationContext context() {
        OperationActor actor = new OperationActor(OperationActorType.USER, "user-1", "Alice", "tenant-1", Map.of());
        OperationActor initiator = new OperationActor(OperationActorType.SERVICE, "gateway", "Gateway", "tenant-1", Map.of());
        OperationSource source = new OperationSource("HTTP", "order-service", "instance-1", "GET /orders", Map.of());
        return new OperationContext(actor, initiator, source, "trace-1", "tenant-1", "request-1", Instant.now(), Map.of());
    }
}
