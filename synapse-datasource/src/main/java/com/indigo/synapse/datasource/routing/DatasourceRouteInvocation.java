package com.indigo.synapse.datasource.routing;

import java.lang.reflect.Method;

/**
 * Resolver 可读取的调用快照。
 *
 * @param method 被调用方法
 * @param target 目标对象
 * @param arguments 参数副本
 */
public record DatasourceRouteInvocation(Method method, Object target, Object[] arguments) {

    public DatasourceRouteInvocation {
        arguments = arguments == null ? new Object[0] : arguments.clone();
    }

    @Override
    public Object[] arguments() {
        return arguments.clone();
    }
}
