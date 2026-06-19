package com.indigo.synapse.datasource.routing;

import com.indigo.synapse.datasource.definition.DatasourceKey;

import java.util.Optional;

/** 根据调用上下文扩展数据源选择规则的端口。 */
public interface DatasourceRouteResolver {

    Optional<DatasourceKey> resolve(DatasourceRouteInvocation invocation);

    /** @return Resolver 顺序，数值越小优先 */
    default int order() {
        return 0;
    }
}
