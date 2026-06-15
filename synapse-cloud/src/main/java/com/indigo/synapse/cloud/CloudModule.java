package com.indigo.synapse.cloud;

/**
 * Synapse Cloud 模块标识。
 *
 * <p>该模块只提供 Spring Cloud / OpenFeign 服务间调用技术适配，不承载 Gateway、注册中心、
 * 配置中心、IAM 或业务 SDK。</p>
 */
public final class CloudModule {

    public static final String NAME = "synapse-cloud";

    private CloudModule() {
    }

    public static String dependsOn() {
        return "synapse-core";
    }
}
