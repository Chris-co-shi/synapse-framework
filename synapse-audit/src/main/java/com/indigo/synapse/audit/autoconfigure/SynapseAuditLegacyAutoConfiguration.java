package com.indigo.synapse.audit.autoconfigure;

import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.audit.port.CompositeAuditLogPort;
import com.indigo.synapse.audit.port.NoopAuditLogPort;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 旧版本地审计输出入口自动配置。
 *
 * @deprecated since 0.1.0，新代码使用 {@link SynapseAuditAutoConfiguration} 中的
 * {@link com.indigo.synapse.audit.publish.AuditPublisher} 链路。
 */
@Deprecated(since = "0.1.0")
@AutoConfiguration(after = SynapseAuditAutoConfiguration.class)
@ConditionalOnProperty(prefix = "synapse.audit", name = "enabled", matchIfMissing = true)
public class SynapseAuditLegacyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditLogPort.class)
    public AuditLogPort synapseNoopAuditLogPort() {
        return new NoopAuditLogPort();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditRecorder synapseAuditRecorder(
            ObjectProvider<AuditLogPort> auditLogPorts,
            AuditEventContextEnricher contextEnricher) {
        List<AuditLogPort> delegates = auditLogPorts.stream().toList();
        if (delegates.size() == 1) {
            return new AuditRecorder(delegates.get(0), contextEnricher);
        }
        return new AuditRecorder(new CompositeAuditLogPort(delegates), contextEnricher);
    }
}
