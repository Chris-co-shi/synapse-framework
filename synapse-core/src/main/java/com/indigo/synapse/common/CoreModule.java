package com.indigo.synapse.common;

/**
 * Synapse 核心模块标识。
 *
 * <p>该类只暴露模块元信息，避免上层模块通过字符串硬编码判断核心模块名称。</p>
 */
public final class CoreModule {

    /**
     * 核心模块名称。
     */
    public static final String NAME = "synapse-core";

    private CoreModule() {
    }
}
