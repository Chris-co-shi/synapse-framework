package com.indigo.synapse.datasource.routing;

import com.indigo.synapse.datasource.definition.DatasourceKey;
import com.indigo.synapse.datasource.definition.DatasourceRegistry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 按“显式 Scope、@UseDatasource、Resolver、primary”顺序选择数据源。
 */
public final class DatasourceRouteSelector {

    private final DatasourceRouteContext routeContext;
    private final DatasourceRegistry registry;
    private final List<DatasourceRouteResolver> resolvers;

    public DatasourceRouteSelector(DatasourceRouteContext routeContext, DatasourceRegistry registry,
                                   Collection<DatasourceRouteResolver> resolvers) {
        this.routeContext = routeContext;
        this.registry = registry;
        this.resolvers = resolvers == null ? List.of() : resolvers.stream()
                .sorted(Comparator.comparingInt(DatasourceRouteResolver::order)).toList();
    }

    /** @return 按固定优先级选择的 key */
    public DatasourceKey select(DatasourceRouteInvocation invocation) {
        return routeContext.current()
                .or(() -> annotationKey(invocation))
                .or(() -> resolvers.stream().map(resolver -> resolver.resolve(invocation))
                        .flatMap(java.util.Optional::stream).findFirst())
                .or(() -> registry.primary().map(definition -> definition.key()))
                .orElseThrow(() -> new IllegalStateException("no datasource route or primary definition available"));
    }

    private static java.util.Optional<DatasourceKey> annotationKey(DatasourceRouteInvocation invocation) {
        if (invocation == null || invocation.method() == null) {
            return java.util.Optional.empty();
        }
        UseDatasource annotation = invocation.method().getAnnotation(UseDatasource.class);
        if (annotation == null && invocation.target() != null) {
            annotation = invocation.target().getClass().getAnnotation(UseDatasource.class);
        }
        return annotation == null ? java.util.Optional.empty()
                : java.util.Optional.of(new DatasourceKey(annotation.value()));
    }
}
