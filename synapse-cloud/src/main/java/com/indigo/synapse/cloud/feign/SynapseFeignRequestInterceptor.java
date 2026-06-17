package com.indigo.synapse.cloud.feign;

import com.indigo.synapse.cloud.autoconfigure.SynapseFeignProperties;
import com.indigo.synapse.cloud.context.HttpHeaderReader;
import com.indigo.synapse.cloud.context.HttpHeaderWriter;
import com.indigo.synapse.cloud.context.OperationContextHttpHeaderCodec;
import com.indigo.synapse.cloud.security.InternalCallSignRequest;
import com.indigo.synapse.cloud.security.InternalCallSigner;
import com.indigo.synapse.cloud.security.NoopInternalCallSigner;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.oauth2.core.token.BearerTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Feign 出站请求 OperationContext 传播拦截器。
 *
 * <p>该拦截器只写入服务间调用技术 Header，不创建 system actor，不传播 roles、permissions、raw token
 * 或业务数据，也不依赖 WebMVC / WebFlux / Security / MQ。</p>
 */
public final class SynapseFeignRequestInterceptor implements RequestInterceptor {

    private final OperationContextProvider contextProvider;
    private final OperationContextHttpHeaderCodec codec;
    private final SynapseFeignProperties properties;
    private final InternalCallSigner signer;
    private final BearerTokenProvider bearerTokenProvider;

    public SynapseFeignRequestInterceptor() {
        this(new DefaultOperationContextProvider(), new OperationContextHttpHeaderCodec(),
                new SynapseFeignProperties(), new NoopInternalCallSigner(), Optional::empty);
    }

    public SynapseFeignRequestInterceptor(
            OperationContextProvider contextProvider,
            OperationContextHttpHeaderCodec codec,
            SynapseFeignProperties properties,
            InternalCallSigner signer
    ) {
        this(contextProvider, codec, properties, signer, Optional::empty);
    }

    public SynapseFeignRequestInterceptor(
            OperationContextProvider contextProvider,
            OperationContextHttpHeaderCodec codec,
            SynapseFeignProperties properties,
            InternalCallSigner signer,
            BearerTokenProvider bearerTokenProvider
    ) {
        if (contextProvider == null) {
            throw new IllegalArgumentException("contextProvider must not be null");
        }
        if (codec == null) {
            throw new IllegalArgumentException("codec must not be null");
        }
        this.contextProvider = contextProvider;
        this.codec = codec;
        this.properties = properties == null ? new SynapseFeignProperties() : properties;
        this.signer = signer == null ? new NoopInternalCallSigner() : signer;
        this.bearerTokenProvider = bearerTokenProvider == null ? Optional::empty : bearerTokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (template == null || !properties.isEnabled()) {
            return;
        }
        boolean override = properties.isOverrideExistingHeaders();
        HttpHeaderReader reader = name -> template.headers().containsKey(name);
        HttpHeaderWriter writer = (name, value) -> {
            if (override && reader.contains(name)) {
                template.removeHeader(name);
            }
            template.header(name, value);
        };
        if (properties.isContextPropagationEnabled()) {
            OperationContext context = contextProvider.current().orElse(null);
            codec.write(context, writer, reader, override);
        }
        if (properties.isInternalSignatureEnabled()) {
            signer.sign(signRequest(template), writer, reader, override);
        }
        if (properties.isBearerTokenRelayEnabled()) {
            relayBearerToken(template, reader, override);
        }
    }

    private void relayBearerToken(RequestTemplate template, HttpHeaderReader reader, boolean override) {
        if (!override && reader.contains("Authorization")) {
            return;
        }
        bearerTokenProvider.currentToken()
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .ifPresent(token -> {
                    if (override && reader.contains("Authorization")) {
                        template.removeHeader("Authorization");
                    }
                    template.header("Authorization", "Bearer " + token);
                });
    }

    private InternalCallSignRequest signRequest(RequestTemplate template) {
        Map<String, Collection<String>> headers = template.headers();
        return new InternalCallSignRequest(template.method(), template.url(), headers);
    }
}
