package com.indigo.synapse.observability;

/** 由各 Framework 模块提供健康快照的扩展端口，不依赖 Spring Boot Actuator。 */
@FunctionalInterface
public interface FrameworkHealthIndicator {
    FrameworkHealthSnapshot health();
}
