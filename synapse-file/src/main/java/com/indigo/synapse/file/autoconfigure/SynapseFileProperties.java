package com.indigo.synapse.file.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * File 模块配置属性。
 *
 * <p>一阶段只提供本地文件存储根目录配置。该配置不表达文件大小限制、文件类型白名单、访问权限、
 * CDN、对象存储桶策略或附件业务表。</p>
 */
@ConfigurationProperties(prefix = "synapse.file")
public class SynapseFileProperties {

    /**
     * 本地文件存储根目录，支持文件系统绝对路径或相对路径。该目录只用于 LocalFileStorage，不代表文件中心。
     */
    private Path localRoot = Path.of(System.getProperty("java.io.tmpdir"), "synapse-file");

    public Path getLocalRoot() {
        return localRoot;
    }

    public void setLocalRoot(Path localRoot) {
        this.localRoot = localRoot;
    }
}
