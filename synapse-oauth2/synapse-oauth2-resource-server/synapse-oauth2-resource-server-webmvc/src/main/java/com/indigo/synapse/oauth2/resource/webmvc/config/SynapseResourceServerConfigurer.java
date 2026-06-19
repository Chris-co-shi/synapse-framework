package com.indigo.synapse.oauth2.resource.webmvc.config;

import com.indigo.synapse.oauth2.resource.webmvc.autoconfigure.SynapseResourceServerProperties;
import com.indigo.synapse.oauth2.resource.webmvc.context.SynapsePrincipalContextBridgeFilter;
import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationConverter;
import com.indigo.synapse.oauth2.resource.webmvc.web.SynapseAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webmvc.web.SynapseBearerAuthenticationEntryPoint;
import com.indigo.synapse.oauth2.resource.webmvc.gatewayproof.GatewayProofVerificationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/**
 * Servlet OAuth2 Resource Server 的默认 HttpSecurity 配置器。
 *
 * <p>该类只负责把框架提供的认证转换器、异常处理器、匿名路径和上下文桥接 Filter 组装进
 * Spring Security，不实现登录、用户查询、授权数据加载或 JWT 签发。</p>
 *
 * <p>默认链路：</p>
 * <pre>
 * permit paths / authenticated policy
 *     -> BearerTokenAuthenticationFilter
 *     -> JWT decoder and validators
 *     -> SynapseJwtAuthenticationConverter
 *     -> SynapsePrincipalContextBridgeFilter
 *     -> Controller / Service
 * </pre>
 *
 * <p>复杂应用提供自定义 {@code SecurityFilterChain} 时，可以显式复用本配置器，而不是复制整套
 * Resource Server 组装逻辑。</p>
 */
public final class SynapseResourceServerConfigurer {

    private final SynapseResourceServerProperties properties;
    private final SynapseJwtAuthenticationConverter authenticationConverter;
    private final SynapseBearerAuthenticationEntryPoint entryPoint;
    private final SynapseAccessDeniedHandler accessDeniedHandler;
    private final SynapsePrincipalContextBridgeFilter bridgeFilter;
    private final GatewayProofVerificationFilter gatewayProofVerificationFilter;

    public SynapseResourceServerConfigurer(
            SynapseResourceServerProperties properties,
            SynapseJwtAuthenticationConverter authenticationConverter,
            SynapseBearerAuthenticationEntryPoint entryPoint,
            SynapseAccessDeniedHandler accessDeniedHandler,
            SynapsePrincipalContextBridgeFilter bridgeFilter) {
        this(properties, authenticationConverter, entryPoint, accessDeniedHandler, bridgeFilter, null);
    }

    public SynapseResourceServerConfigurer(
            SynapseResourceServerProperties properties,
            SynapseJwtAuthenticationConverter authenticationConverter,
            SynapseBearerAuthenticationEntryPoint entryPoint,
            SynapseAccessDeniedHandler accessDeniedHandler,
            SynapsePrincipalContextBridgeFilter bridgeFilter,
            GatewayProofVerificationFilter gatewayProofVerificationFilter) {
        this.properties = properties;
        this.authenticationConverter = authenticationConverter;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.bridgeFilter = bridgeFilter;
        this.gatewayProofVerificationFilter = gatewayProofVerificationFilter;
    }

    /**
     * 将 Synapse 默认 Resource Server 策略应用到给定 HttpSecurity。
     *
     * <p>配置顺序本身表达了边界：先定义无状态和访问策略，再接入 OAuth2 JWT 转换，最后把
     * Bridge Filter 放到 {@link BearerTokenAuthenticationFilter} 之后，使其只能读取已经认证完成的主体。</p>
     *
     * @param http 消费方正在构建的 HttpSecurity
     * @return 同一个 HttpSecurity，便于消费方继续追加自定义配置
     * @throws Exception Spring Security 配置失败
     */
    public HttpSecurity configure(HttpSecurity http) throws Exception {
        if (!properties.isCsrfEnabled()) {
            http.csrf(csrf -> csrf.disable());
        }

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler));
        http.authorizeHttpRequests(registry -> {
            properties.getPermitPaths().forEach(path -> registry.requestMatchers(path).permitAll());
            registry.anyRequest().authenticated();
        });
        http.oauth2ResourceServer(resourceServer ->
                resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));
        if (gatewayProofVerificationFilter != null) {
            http.addFilterBefore(gatewayProofVerificationFilter, BearerTokenAuthenticationFilter.class);
        }
        http.addFilterAfter(bridgeFilter, BearerTokenAuthenticationFilter.class);
        return http;
    }
}
