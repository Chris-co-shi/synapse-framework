package com.indigo.synapse.security.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Synapse Security 自动配置属性。
 *
 * <p>trusted-header 默认关闭，避免业务服务仅引入模块就强制拦截所有请求。
 * 开启签名校验时必须提供共享密钥，密钥只用于本地 HMAC 校验，不应写入日志。</p>
 */
@ConfigurationProperties(prefix = "synapse.security")
public class SynapseSecurityProperties {

    private final TrustedHeader trustedHeader = new TrustedHeader();
    private final Permission permission = new Permission();

    public TrustedHeader getTrustedHeader() {
        return trustedHeader;
    }

    public Permission getPermission() {
        return permission;
    }

    /**
     * 校验 trusted-header 运行配置。
     */
    public void validateTrustedHeaderConfiguration() {
        trustedHeader.validate();
    }

    /**
     * trusted-header 请求入口配置。
     */
    public static class TrustedHeader {

        /**
         * 是否启用 trusted-header 解析。默认关闭，避免业务服务仅引入模块就强制拦截所有请求。
         */
        private boolean enabled;

        /**
         * 是否启用 trusted-header HMAC 签名校验。启用时必须同时提供 `secret`。
         */
        private boolean signatureEnabled = true;

        /**
         * trusted-header HMAC 共享密钥。该值属于敏感配置，不要写入日志或提交到代码仓库。
         */
        private String secret;

        /**
         * 请求时间戳允许偏差，使用 Spring Boot Duration 格式，例如 `30s`、`5m`；不能为负数。
         */
        private Duration timestampTolerance = Duration.ofSeconds(300);

        /**
         * 是否在 trusted-header 校验失败时快速失败。关闭后可由消费方自定义后续处理策略。
         */
        private boolean failFast = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSignatureEnabled() {
            return signatureEnabled;
        }

        public void setSignatureEnabled(boolean signatureEnabled) {
            this.signatureEnabled = signatureEnabled;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getTimestampTolerance() {
            return timestampTolerance;
        }

        public void setTimestampTolerance(Duration timestampTolerance) {
            this.timestampTolerance = timestampTolerance;
        }

        public boolean isFailFast() {
            return failFast;
        }

        public void setFailFast(boolean failFast) {
            this.failFast = failFast;
        }

        private void validate() {
            if (timestampTolerance == null) {
                throw new IllegalStateException("synapse.security.trusted-header.timestamp-tolerance must not be null");
            }
            if (timestampTolerance.isNegative()) {
                throw new IllegalStateException("synapse.security.trusted-header.timestamp-tolerance must not be negative");
            }
            if (enabled && signatureEnabled && (secret == null || secret.isBlank())) {
                throw new IllegalStateException("synapse.security.trusted-header.secret must not be blank when signature is enabled");
            }
        }
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
}
