package com.indigo.synapse.oauth2.resource.webmvc.context;

import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationToken;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.context.internal.SecurityContextScope;
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
 * 将 Spring Security Authentication 桥接到 Synapse SecurityContext 和 OperationContext。
 *
 * <p>该 Filter 必须位于 Spring Security 的 Bearer Token 认证过滤器之后。此时
 * {@link SecurityContextHolder} 中已经存在完成 JWT 转换的 {@link SynapseJwtAuthenticationToken}，
 * Filter 可以直接提取其中的 {@link AuthenticatedPrincipal}，无需再次解析 claims。</p>
 *
 * <p>建立 Synapse scope 后，后续 Controller、Service、data、audit 和 mq 代码都可以读取当前主体或
 * OperationContext。scope 使用 try-with-resources 关闭，确保请求正常结束或异常退出时都能恢复进入请求前的
 * 上下文，避免 Servlet 线程池复用造成身份污染。</p>
 *
 * <p>如果当前 Authentication 不是 Synapse JWT token，本 Filter 不猜测主体类型，也不修改 Synapse 上下文。</p>
 */
public final class SynapseSecurityContextBridgeFilter extends OncePerRequestFilter {

    /**
     * 在当前 HTTP 请求作用域内建立并清理 Synapse 安全上下文。
     */
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

        try (SecurityContextScope ignored = SecurityContextBinder.bind(principal)) {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 只接受经过 Synapse JWT converter 创建的 Authentication，避免对未知 Authentication 做隐式转换。
     */
    private AuthenticatedPrincipal principal(Authentication authentication) {
        if (authentication instanceof SynapseJwtAuthenticationToken token) {
            return token.authenticatedPrincipal();
        }
        return null;
    }
}
