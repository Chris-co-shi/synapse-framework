package com.indigo.synapse.webmvc.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Servlet MVC 统一错误响应写出器。
 *
 * <p>该组件只依赖 webmvc 的 {@link WebErrorResponse} 和 Result 结构，不依赖 Security/OAuth2。</p>
 */
public final class WebErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public WebErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public void write(HttpServletResponse response, WebErrorResponse errorResponse) throws IOException {
        response.resetBuffer();
        response.setStatus(errorResponse.status());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse.body());
    }
}
