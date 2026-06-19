package com.indigo.synapse.datasource.routing;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * dynamic-datasource 路由栈的可关闭作用域。
 *
 * <p>关闭时只弹出本作用域压入的 key，支持嵌套恢复；实例不可跨线程使用。</p>
 */
public final class DatasourceRouteScope implements AutoCloseable {

    private final Thread owner = Thread.currentThread();
    private final AtomicBoolean closed = new AtomicBoolean();

    DatasourceRouteScope() {
    }

    @Override
    public void close() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("DatasourceRouteScope must be closed on the creating thread");
        }
        if (closed.compareAndSet(false, true)) {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
