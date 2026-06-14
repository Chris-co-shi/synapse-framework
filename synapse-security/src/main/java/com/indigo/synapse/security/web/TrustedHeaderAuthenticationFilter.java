package com.indigo.synapse.security.web;

import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import com.indigo.synapse.security.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.header.SecurityHeaders;
import com.indigo.synapse.security.header.TrustedHeaderAuthenticatedUserResolver;
import com.indigo.synapse.security.header.TrustedHeaderSignatureVerifier;
import com.indigo.synapse.security.header.TrustedHeaderTimestampValidator;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.Clock;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 基于 trusted-header 的轻量认证 Filter。
 *
 * <p>该 Filter 面向位于 Gateway / IAM 后方的业务服务：它只恢复可信 Header 中的
 * {@link AuthenticatedUser} 并写入 {@link SecurityContext}，不做登录、不验签认证令牌、
 * 不创建 Spring Security 过滤链。</p>
 */
public class TrustedHeaderAuthenticationFilter implements Filter {

    private static final List<String> TRUSTED_HEADERS = List.of(
            SecurityHeaders.USER_ID,
            SecurityHeaders.USERNAME,
            SecurityHeaders.TENANT_ID,
            SecurityHeaders.ROLES,
            SecurityHeaders.PERMISSIONS,
            SecurityHeaders.TRACE_ID,
            SecurityHeaders.REQUEST_ID,
            SecurityHeaders.SOURCE,
            SecurityHeaders.SIGNATURE,
            SecurityHeaders.TIMESTAMP,
            SecurityHeaders.NONCE
    );

    private final SynapseSecurityProperties properties;
    private final TrustedHeaderAuthenticatedUserResolver authenticatedUserResolver;
    private final TrustedHeaderSignatureVerifier signatureVerifier;
    private final TrustedHeaderTimestampValidator timestampValidator;
    private final Clock clock;

    public TrustedHeaderAuthenticationFilter(
            SynapseSecurityProperties properties,
            TrustedHeaderAuthenticatedUserResolver authenticatedUserResolver,
            TrustedHeaderSignatureVerifier signatureVerifier,
            TrustedHeaderTimestampValidator timestampValidator) {
        this(properties, authenticatedUserResolver, signatureVerifier, timestampValidator, Clock.systemUTC());
    }

    public TrustedHeaderAuthenticationFilter(
            SynapseSecurityProperties properties,
            TrustedHeaderAuthenticatedUserResolver authenticatedUserResolver,
            TrustedHeaderSignatureVerifier signatureVerifier,
            TrustedHeaderTimestampValidator timestampValidator,
            Clock clock) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        if (authenticatedUserResolver == null) {
            throw new IllegalArgumentException("authenticatedUserResolver must not be null");
        }
        if (signatureVerifier == null) {
            throw new IllegalArgumentException("signatureVerifier must not be null");
        }
        if (timestampValidator == null) {
            throw new IllegalArgumentException("timestampValidator must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.properties = properties;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.signatureVerifier = signatureVerifier;
        this.timestampValidator = timestampValidator;
        this.clock = clock;
    }

    /**
     * 恢复 trusted-header 安全上下文，并确保请求结束或异常时清理当前 Filter 绑定的上下文。
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SynapseSecurityProperties.TrustedHeader trustedHeader = properties.getTrustedHeader();
        if (!trustedHeader.isEnabled() || !(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Map<String, String> headers = extractTrustedHeaders(httpRequest);
            authenticate(headers, trustedHeader);
            chain.doFilter(request, response);
        } catch (SynapseAuthenticationException exception) {
            if (trustedHeader.isFailFast()) {
                throw exception;
            }
            chain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }

    private void authenticate(Map<String, String> headers, SynapseSecurityProperties.TrustedHeader trustedHeader) {
        timestampValidator.validate(headers, trustedHeader.getTimestampTolerance(), clock);
        if (trustedHeader.isSignatureEnabled()
                && !signatureVerifier.verify(headers, trustedHeader.getSecret())) {
            throw new SynapseAuthenticationException(SecurityErrorCode.SECURITY_INVALID_SIGNATURE);
        }
        SecurityContext.set(authenticatedUserResolver.resolveAuthenticatedUser(headers));
    }

    private static Map<String, String> extractTrustedHeaders(HttpServletRequest request) {
        Map<String, String> byLowerCaseName = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            byLowerCaseName.put(name.toLowerCase(Locale.ROOT), trimToNull(request.getHeader(name)));
        }

        Map<String, String> headers = new HashMap<>();
        for (String header : TRUSTED_HEADERS) {
            String value = request.getHeader(header);
            if (value == null) {
                value = byLowerCaseName.get(header.toLowerCase(Locale.ROOT));
            }
            value = trimToNull(value);
            if (value != null) {
                headers.put(header, value);
            }
        }
        return headers;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
