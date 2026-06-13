package com.indigo.synapse.tenant.ignore;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantIgnoreScopeTest {

    @Test
    void shouldOpenAndClearTenantIgnoreStrategy() {
        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("demo.Mapper.selectById"));

        try (TenantIgnoreScope ignored = TenantIgnoreScope.open()) {
            assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("demo.Mapper.selectById"));
        }

        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("demo.Mapper.selectById"));
    }
}
