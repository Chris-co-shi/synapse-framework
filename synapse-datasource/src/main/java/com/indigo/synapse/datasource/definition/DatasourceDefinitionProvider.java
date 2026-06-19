package com.indigo.synapse.datasource.definition;

import java.util.Collection;

/** 外部配置源向 Framework 提供数据源定义的端口。 */
public interface DatasourceDefinitionProvider {

    /** @return 当前定义快照，不得包含明文密码 */
    Collection<DatasourceDefinition> load();

    /** @return Provider 顺序，数值越小优先级越高 */
    default int order() {
        return 0;
    }
}
