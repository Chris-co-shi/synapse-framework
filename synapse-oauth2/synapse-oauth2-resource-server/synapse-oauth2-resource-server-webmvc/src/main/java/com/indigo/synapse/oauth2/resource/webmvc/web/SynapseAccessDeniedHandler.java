package com.indigo.synapse.oauth2.resource.webmvc.web;

import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Resource Server 403 写出器。
 */
public final class SynapseAccessDeniedHandler implements AccessDeniedHandler {

    private final WebExceptionResponseFactory responseFactory;
    private final WebErrorResponseWriter responseWriter;

    public SynapseAccessDeniedHandler(WebExceptionResponseFactory responseFactory, WebErrorResponseWriter responseWriter) {
        this.responseFactory = responseFactory;
        this.responseWriter = responseWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        responseWriter.write(response, responseFactory.mvc(new SynapseAccessDeniedException()));
    }
}
