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

    public Permission getPermission() {
        return permission;
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
