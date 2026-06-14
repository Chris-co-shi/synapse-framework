package com.indigo.synapse.file.autoconfigure;

import com.indigo.synapse.file.storage.FileStorage;
import com.indigo.synapse.file.storage.local.LocalFileStorage;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * File 模块自动配置。
 *
 * <p>该配置只提供默认 {@link FileStorage} Bean，并在消费方没有自定义实现时使用本地文件系统实现。
 * 它不注册上传/下载 Controller，不创建附件表，不实现文件权限，也不绑定 OSS、S3、MinIO 等外部对象存储。</p>
 *
 * <p>生产系统通常应根据自身基础设施提供自定义 FileStorage，例如对象存储、私有文件服务或平台文件中心 adapter。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseFileProperties.class)
public class SynapseFileAutoConfiguration {

    /**
     * 创建默认文件存储实现。
     *
     * <p>默认实现使用本地文件系统，适合开发、测试或单机轻量场景。消费方提供 FileStorage Bean 时不会覆盖。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public FileStorage synapseFileStorage(SynapseFileProperties properties) {
        return new LocalFileStorage(properties.getLocalRoot());
    }
}
