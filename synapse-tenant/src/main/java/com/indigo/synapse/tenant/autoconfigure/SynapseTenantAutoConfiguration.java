package com.indigo.synapse.tenant.autoconfigure;

import com.indigo.synapse.tenant.aop.TenantIgnoreAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SynapseTenantAutoConfiguration {

    @Bean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }
}
