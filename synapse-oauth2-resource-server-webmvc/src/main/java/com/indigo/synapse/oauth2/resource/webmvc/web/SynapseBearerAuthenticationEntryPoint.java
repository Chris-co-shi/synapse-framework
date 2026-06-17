package com.indigo.synapse.oauth2.resource.webmvc.web;

import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Resource Server 401 写出器。
 */
public final class SynapseBearerAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final WebExceptionResponseFactory responseFactory;
    private final WebErrorResponseWriter responseWriter;

    public SynapseBearerAuthenticationEntryPoint(
            WebExceptionResponseFactory responseFactory,
            WebErrorResponseWriter responseWriter) {
        this.responseFactory = responseFactory;
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        responseWriter.write(response, responseFactory.mvc(new SynapseAuthenticationException(
                OAuth2ErrorCode.OAUTH2_INVALID_TOKEN,
                authException == null ? OAuth2ErrorCode.OAUTH2_INVALID_TOKEN.message() : authException.getMessage()
        )));
    }
}
