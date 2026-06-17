package com.indigo.synapse.security.web;

import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.context.SecurityContextScope;
import com.indigo.synapse.security.exception.SecurityErrorCode;
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
 * <p>该 Filter 面向位于 Gateway / IAM 后方的业务服务：它只校验并恢复可信 Header 中的
 * {@link AuthenticatedUser}，不做登录、不验签 OAuth2/JWT token、不创建 Spring Security 过滤链。</p>
 *
 * <p>Filter 使用 {@link SecurityContextScope} 管理生命周期：请求结束或异常时恢复进入 Filter 前的
 * SecurityContext 与 OperationContext，而不是无条件清除外层上下文。认证失败只在认证阶段处理，
 * 下游 Filter 或 Controller 抛出的认证异常不会被本 Filter 捕获或导致 FilterChain 重复执行。</p>
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
     * 校验 trusted-header，并在独立安全上下文作用域内执行后续 FilterChain。
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SynapseSecurityProperties.TrustedHeader trustedHeader = properties.getTrustedHeader();
        if (!trustedHeader.isEnabled() || !(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        AuthenticatedUser authenticatedUser;
        try {
            Map<String, String> headers = extractTrustedHeaders(httpRequest);
            authenticatedUser = authenticate(headers, trustedHeader);
        } catch (SynapseAuthenticationException exception) {
            if (trustedHeader.isFailFast()) {
                throw exception;
            }
            doFilterWithSecurityContext(request, response, chain, null);
            return;
        }

        doFilterWithSecurityContext(request, response, chain, authenticatedUser);
    }

    private AuthenticatedUser authenticate(
            Map<String, String> headers,
            SynapseSecurityProperties.TrustedHeader trustedHeader) {
        timestampValidator.validate(headers, trustedHeader.getTimestampTolerance(), clock);
        if (trustedHeader.isSignatureEnabled()
                && !signatureVerifier.verify(headers, trustedHeader.getSecret())) {
            throw new SynapseAuthenticationException(SecurityErrorCode.SECURITY_INVALID_SIGNATURE);
        }
        return authenticatedUserResolver.resolveAuthenticatedUser(headers);
    }

    private static void doFilterWithSecurityContext(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain,
            AuthenticatedUser authenticatedUser) throws IOException, ServletException {
        try (SecurityContextScope ignored = SecurityContext.scope(authenticatedUser)) {
            chain.doFilter(request, response);
        }
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
