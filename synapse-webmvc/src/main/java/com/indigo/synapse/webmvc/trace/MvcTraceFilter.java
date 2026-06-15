package com.indigo.synapse.webmvc.trace;

import com.indigo.synapse.webmvc.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet MVC trace filter。
 *
 * <p>该 Filter 在请求进入业务 Controller 前建立 Web 请求上下文和 traceId，并在请求结束后清理。
 * 它只处理 Web trace，不负责认证、授权或 OperationContext。</p>
 */
public final class MvcTraceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        RequestContext requestContext = WebTraceLifecycle.start(
                request.getHeader(TraceHeaders.TRACE_ID),
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
        response.setHeader(TraceHeaders.TRACE_ID, requestContext.traceId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            WebTraceLifecycle.end();
        }
    }
}
