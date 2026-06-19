package com.indigo.synapse.oauth2.resource.webflux.gatewayproof;

import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationResult;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationStatus;
import com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter;
import com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory;
import com.indigo.synapse.web.core.trace.TraceIdGenerator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive GatewayProof 403 响应写出器。
 *
 * <p>该类复用 synapse-webflux 统一 Result 写出规则，不泄露 canonical string、secret 或 token 指纹。
 * 它不参与 JWT 认证和 Reactor 安全上下文建立。</p>
 */
public final class GatewayProofServerAccessDeniedHandler {

    private final WebFluxExceptionResponseFactory responseFactory;
    private final ReactiveWebErrorResponseWriter responseWriter;

    public GatewayProofServerAccessDeniedHandler(
            WebFluxExceptionResponseFactory responseFactory,
            ReactiveWebErrorResponseWriter responseWriter
    ) {
        this.responseFactory = responseFactory;
        this.responseWriter = responseWriter;
    }

    /**
     * 写出 GatewayProof 拒绝响应。
     *
     * @param exchange WebFlux exchange
     * @param result 验证结果
     * @return 写出完成信号
     */
    public Mono<Void> handle(ServerWebExchange exchange, GatewayProofVerificationResult result) {
        String traceId = TraceIdGenerator.generate();
        ErrorCode errorCode = map(result.status());
        return responseWriter.write(
                exchange,
                responseFactory.from(new SynapseAccessDeniedException(errorCode, errorCode.message()), traceId),
                traceId
        );
    }

    private ErrorCode map(GatewayProofVerificationStatus status) {
        return switch (status) {
            case MISSING -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_MISSING;
            case UNSUPPORTED_VERSION -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_UNSUPPORTED_VERSION;
            case UNKNOWN_GATEWAY -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_UNKNOWN_GATEWAY;
            case EXPIRED -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_EXPIRED;
            case REPLAYED -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_REPLAYED;
            case CONFIGURATION_INVALID -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_CONFIGURATION_INVALID;
            case INVALID_SIGNATURE, INVALID_REQUEST -> SecurityErrorCode.SECURITY_GATEWAY_PROOF_INVALID;
            case SUCCESS -> SecurityErrorCode.SECURITY_PERMISSION_DENIED;
        };
    }
}
