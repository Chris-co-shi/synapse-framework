package com.indigo.synapse.web.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.core.exception.SynapseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private final ObjectMapper objectMapper;

    public SynapseExceptionBridgeFilter(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SynapseException exception) {
            if (response.isCommitted()) {
                throw exception;
            }
            write(response, WebExceptionResponseFactory.mvc(exception));
        }
    }

    private void write(HttpServletResponse response, WebErrorResponse errorResponse) throws IOException {
        response.resetBuffer();
        response.setStatus(errorResponse.status());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse.body());
    }
}
