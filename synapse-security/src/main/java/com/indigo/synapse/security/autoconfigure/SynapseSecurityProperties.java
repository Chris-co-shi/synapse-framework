package com.indigo.synapse.security.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Synapse Security 自动配置属性。
 *
 * <p>这里只维护 Web 无关的权限检查配置。认证协议、Bearer Token 验证和请求入口配置
 * 由 OAuth2 Resource Server 等专用适配模块负责。</p>
 */
@ConfigurationProperties(prefix = "synapse.security")
public class SynapseSecurityProperties {

    private final Permission permission = new Permission();
    private final GatewayProof gatewayProof = new GatewayProof();

    public Permission getPermission() {
        return permission;
    }

    public GatewayProof getGatewayProof() {
        return gatewayProof;
    }

    /**
     * 权限检查配置。
     */
    public static class Permission {

        /**
         * 是否启用 `@RequirePermission` 注解适配。关闭后不会注册默认权限注解切面。
         */
        private boolean annotationEnabled = true;

        public boolean isAnnotationEnabled() {
            return annotationEnabled;
        }

        public void setAnnotationEnabled(boolean annotationEnabled) {
            this.annotationEnabled = annotationEnabled;
        }
    }

    /**
     * GatewayProof 可信入口防伪配置。
     *
     * <p>该配置只控制 GatewayProof 协议启用和部署策略，不改变 JWT Resource Server 的身份校验职责。
     * Header 名称、v1 canonicalization、HMAC-SHA256 算法和时间戳单位均为固定协议。</p>
     */
    public static class GatewayProof {

        /**
         * 是否启用 GatewayProof 入站校验。默认关闭，避免引入模块后影响本地开发。
         */
        private boolean enabled;
        /**
         * 启用后是否要求每个非 permit path 请求都携带有效 GatewayProof。
         */
        private boolean required = true;
        /**
         * 当前服务信任的 Gateway 标识。
         */
        private String gatewayId = "synapse-gateway";
        /**
         * HMAC secret。必须通过环境变量或 Secret Manager 注入，禁止写入日志。
         */
        private String secret = "";
        /**
         * GatewayProof 时间戳允许偏移。
         */
        private java.time.Duration timestampSkew = java.time.Duration.ofSeconds(60);
        /**
         * 是否启用 nonce 重放保护。启用后必须提供真实 GatewayProofReplayStore。
         */
        private boolean replayProtectionEnabled;
        /**
         * 配置非法时是否启动失败。
         */
        private boolean failFast = true;
        /**
         * 跳过 GatewayProof 校验的技术路径。
         */
        private java.util.List<String> permitPaths = new java.util.ArrayList<>(
                java.util.List.of("/actuator/health", "/error")
        );

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getGatewayId() {
            return gatewayId;
        }

        public void setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public java.time.Duration getTimestampSkew() {
            return timestampSkew;
        }

        public void setTimestampSkew(java.time.Duration timestampSkew) {
            this.timestampSkew = timestampSkew;
        }

        public boolean isReplayProtectionEnabled() {
            return replayProtectionEnabled;
        }

        public void setReplayProtectionEnabled(boolean replayProtectionEnabled) {
            this.replayProtectionEnabled = replayProtectionEnabled;
        }

        public boolean isFailFast() {
            return failFast;
        }

        public void setFailFast(boolean failFast) {
            this.failFast = failFast;
        }

        public java.util.List<String> getPermitPaths() {
            return permitPaths;
        }

        public void setPermitPaths(java.util.List<String> permitPaths) {
            this.permitPaths = permitPaths == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(permitPaths);
        }
    }
}
