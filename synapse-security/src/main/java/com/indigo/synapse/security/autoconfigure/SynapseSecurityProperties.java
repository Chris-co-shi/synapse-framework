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

    public TrustedHeader getTrustedHeader() {
        return trustedHeader;
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

        private boolean enabled;
        private boolean signatureEnabled = true;
        private String secret;
        private Duration timestampTolerance = Duration.ofSeconds(300);
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
}
