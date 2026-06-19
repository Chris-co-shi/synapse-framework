package com.indigo.synapse.webmvc.openapi;

/**
 * OpenAPI 默认展示属性。
 *
 * <p>该对象只描述 synapse-webmvc 对 OpenAPI 可见性的默认判断输入，不负责注册 springdoc、Swagger UI
 * 或任何文档端点。是否真正启用 OpenAPI 仍由业务系统依赖和配置决定。</p>
 *
 * @param enabled 是否允许展示
 * @param title 文档标题
 * @param version 文档版本
 */
public record OpenApiProperties(boolean enabled, String title, String version) {

    /**
     * 返回 Synapse 默认 OpenAPI 属性。
     */
    public static OpenApiProperties defaults() {
        return new OpenApiProperties(true, "Synapse Framework API", "0.1.0");
    }

    public OpenApiProperties {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
    }

    /**
     * 返回禁用展示后的新属性对象。
     */
    public OpenApiProperties disabled() {
        return new OpenApiProperties(false, title, version);
    }
}
