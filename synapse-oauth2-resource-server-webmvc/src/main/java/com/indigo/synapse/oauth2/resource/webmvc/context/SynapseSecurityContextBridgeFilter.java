package com.indigo.synapse.oauth2.resource.webmvc.context;

import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationToken;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.context.SecurityContextScope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 将 Spring Security Authentication 桥接到 Synapse SecurityContext / OperationContext。
 */
public final class SynapseSecurityContextBridgeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedPrincipal principal = principal(authentication);
        if (principal == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try (SecurityContextScope ignored = SecurityContext.openScope(principal)) {
            filterChain.doFilter(request, response);
        }
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        if (authentication instanceof SynapseJwtAuthenticationToken token) {
            return token.authenticatedPrincipal();
        }
        return null;
    }
}
