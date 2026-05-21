package com.indigo.synapse.web.trace;

import com.indigo.synapse.web.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
