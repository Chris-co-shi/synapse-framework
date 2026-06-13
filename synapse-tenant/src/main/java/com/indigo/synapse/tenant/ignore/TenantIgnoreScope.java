package com.indigo.synapse.tenant.ignore;


import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;

public final class TenantIgnoreScope implements AutoCloseable {

    private boolean closed;

    private TenantIgnoreScope() {
    }

    public static TenantIgnoreScope open() {
        InterceptorIgnoreHelper.handle(IgnoreStrategy.builder()
                .tenantLine(Boolean.TRUE)
                .build());
        return new TenantIgnoreScope();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        InterceptorIgnoreHelper.clearIgnoreStrategy();
    }
}
