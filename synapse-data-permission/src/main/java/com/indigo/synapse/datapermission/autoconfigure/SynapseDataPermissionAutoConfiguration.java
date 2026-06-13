package com.indigo.synapse.datapermission.autoconfigure;

import com.indigo.synapse.datapermission.model.DataPermissionPolicy;
import com.indigo.synapse.datapermission.resolver.DataPermissionResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SynapseDataPermissionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataPermissionResolver.class)
    public DataPermissionResolver synapseDataPermissionResolver() {
        return (subjectId, permissionKey) -> DataPermissionPolicy.all();
    }
}
