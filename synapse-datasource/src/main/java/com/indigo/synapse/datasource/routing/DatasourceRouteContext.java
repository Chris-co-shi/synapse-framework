package com.indigo.synapse.datasource.routing;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.indigo.synapse.datasource.definition.DatasourceKey;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * 数据源显式路由上下文。
 *
 * <p>本类型不实现路由引擎，只管理 dynamic-datasource 官方 ThreadLocal 栈。路由必须在事务开始前
 * 建立；活动事务内仅允许重复选择当前 key，禁止切换或首次补选。</p>
 */
public final class DatasourceRouteContext {

    private final BooleanSupplier transactionActive;

    public DatasourceRouteContext() {
        this(TransactionSynchronizationManager::isActualTransactionActive);
    }

    /** 测试或特殊运行时可注入事务状态读取器。 */
    public DatasourceRouteContext(BooleanSupplier transactionActive) {
        this.transactionActive = transactionActive;
    }

    /** @return 当前 dynamic-datasource key */
    public Optional<DatasourceKey> current() {
        return Optional.ofNullable(DynamicDataSourceContextHolder.peek()).map(DatasourceKey::new);
    }

    /**
     * 打开显式路由作用域。
     *
     * @param key 已注册的数据源 key
     * @return 必须在当前线程关闭的作用域
     * @throws DatasourceTransactionSwitchException 活动事务中发生首次选择或切换时
     */
    public DatasourceRouteScope open(DatasourceKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        String current = DynamicDataSourceContextHolder.peek();
        if (transactionActive.getAsBoolean() && !key.value().equals(current)) {
            throw new DatasourceTransactionSwitchException(current, key.value());
        }
        DynamicDataSourceContextHolder.push(key.value());
        return new DatasourceRouteScope();
    }

    /** 清理当前线程全部路由状态，仅供请求/任务边界兜底。 */
    public void clear() {
        DynamicDataSourceContextHolder.clear();
    }
}
