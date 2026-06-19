package com.indigo.synapse.datasource.routing;

/** 活动本地事务中尝试切换到其他数据源时抛出的异常。 */
public final class DatasourceTransactionSwitchException extends IllegalStateException {

    public DatasourceTransactionSwitchException(String current, String requested) {
        super("Datasource must be selected before transaction starts: current=%s, requested=%s"
                .formatted(current, requested));
    }
}
