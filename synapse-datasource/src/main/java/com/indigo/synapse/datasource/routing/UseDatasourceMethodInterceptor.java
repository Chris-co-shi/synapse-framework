package com.indigo.synapse.datasource.routing;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 将 {@link UseDatasource} 方法注解适配为可关闭路由作用域。
 *
 * <p>作用域在目标方法返回或抛出异常时都会关闭。事务切面必须在本拦截器之后启动，保证连接获取前
 * 已选定数据源。</p>
 */
public final class UseDatasourceMethodInterceptor implements MethodInterceptor {

    private final DatasourceRouteSelector selector;
    private final DatasourceRouteContext routeContext;

    public UseDatasourceMethodInterceptor(DatasourceRouteSelector selector, DatasourceRouteContext routeContext) {
        this.selector = selector;
        this.routeContext = routeContext;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        DatasourceRouteInvocation routeInvocation = new DatasourceRouteInvocation(
                invocation.getMethod(), invocation.getThis(), invocation.getArguments());
        try (DatasourceRouteScope ignored = routeContext.open(selector.select(routeInvocation))) {
            return invocation.proceed();
        }
    }
}
