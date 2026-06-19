package com.indigo.synapse.webmvc.exception;

import com.indigo.synapse.core.exception.SynapseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 将 Servlet Filter 阶段抛出的 {@link SynapseException} 桥接为统一响应。
 *
 * <p>MVC 的 {@code @RestControllerAdvice} 只能处理进入 DispatcherServlet 后的异常。
 * trusted-header 等前置 Filter 抛出的框架异常需要由更外层 Filter 捕获，并复用
 * {@link WebExceptionResponseFactory} 写出与 Controller 异常一致的 Result 结构。</p>
 */
public final class SynapseExceptionBridgeFilter extends OncePerRequestFilter {

    /**
     * 必须早于 security trusted-header Filter，才能包住后续 FilterChain。
     */
    public static final int ORDER = -200;

    private final WebErrorResponseWriter responseWriter;
    private final WebExceptionResponseFactory responseFactory;

    public SynapseExceptionBridgeFilter(
            WebErrorResponseWriter responseWriter,
            WebExceptionResponseFactory responseFactory) {
        this.responseWriter = Objects.requireNonNull(responseWriter, "responseWriter must not be null");
        this.responseFactory = Objects.requireNonNull(responseFactory, "responseFactory must not be null");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SynapseException exception) {
            if (response.isCommitted()) {
                throw exception;
            }

            responseWriter.write(response, responseFactory.mvc(exception));
        }
    }
}
