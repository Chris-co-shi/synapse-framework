package com.indigo.synapse.audit.autoconfigure;

import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.audit.port.CompositeAuditLogPort;
import com.indigo.synapse.audit.port.NoopAuditLogPort;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class SynapseAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditLogPort.class)
    public AuditLogPort synapseNoopAuditLogPort() {
        return new NoopAuditLogPort();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditRecorder synapseAuditRecorder(ObjectProvider<AuditLogPort> auditLogPorts) {
        List<AuditLogPort> delegates = auditLogPorts.stream().toList();
        if (delegates.size() == 1) {
            return new AuditRecorder(delegates.get(0));
        }
        return new AuditRecorder(new CompositeAuditLogPort(delegates));
    }
}
