package com.indigo.synapse.tenant.aop;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.indigo.synapse.tenant.annotation.TenantIgnore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantIgnoreAspectTest {

    @AfterEach
    void tearDown() {
        InterceptorIgnoreHelper.clearIgnoreStrategy();
    }

    @Test
    void shouldApplyTenantIgnoreWithinAnnotatedMethod() {
        ProbeService target = new ProbeService();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(new TenantIgnoreAspect());
        ProbeService proxy = proxyFactory.getProxy();

        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("demo.Mapper.find"));

        proxy.ignored();

        assertTrue(target.ignoredState);
        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("demo.Mapper.find"));
    }

    static class ProbeService {

        private boolean ignoredState;

        @TenantIgnore
        void ignored() {
            ignoredState = InterceptorIgnoreHelper.willIgnoreTenantLine("demo.Mapper.find");
        }
    }
}
