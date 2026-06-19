package com.indigo.synapse.web.core;

import com.indigo.synapse.core.CoreModule;

/**
 * Web 技术栈共享模块标识。
 */
public final class WebCoreModule {

    public static final String NAME = "synapse-web-core";

    private WebCoreModule() {
    }

    /**
     * 返回直接依赖的基础模块。
     */
    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
