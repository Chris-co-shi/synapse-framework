package com.indigo.synapse.oauth2.resource.webmvc.gatewayproof;

import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationResult;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationStatus;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet GatewayProof 403 响应写出器。
 *
 * <p>该类复用 synapse-webmvc 统一 Result 写出规则，不暴露 canonical string、secret 或 token 指纹。
 * 它只处理 GatewayProof 拒绝结果，不参与 JWT 认证和 CurrentPrincipalContext 建立。</p>
 */
public final class GatewayProofAccessDeniedHandler {

    private final WebExceptionResponseFactory responseFactory;
    private final WebErrorResponseWriter responseWriter;

    public GatewayProofAccessDeniedHandler(
            WebExceptionResponseFactory responseFactory,
            WebErrorResponseWriter responseWriter
    ) {
        this.responseFactory = responseFactory;
        this.responseWriter = responseWriter;
    }

    /**
     * 写出 GatewayProof 拒绝响应。
     *
     * @param response Servlet response
     * @param result 验证结果
     * @throws IOException 写出失败
     */
    public void handle(HttpServletResponse response, GatewayProofVerificationResult result) throws IOException {
        ErrorCode errorCode = map(result.status());
        responseWriter.write(response, responseFactory.mvc(new SynapseAccessDeniedException(errorCode, errorCode.message())));
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
