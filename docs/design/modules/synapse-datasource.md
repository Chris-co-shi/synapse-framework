# synapse-datasource 设计说明

## 1. 模块使命

`synapse-datasource` 是数据源治理模块，不是 ORM 模块。

它基于运行时 `DatasourceInventory` 建立数据源描述符、数据库类型识别、真实角色探测、健康状态机、安全检查、读库负载均衡和路由决策模型。

## 2. 边界

负责：

- dynamic-datasource 运行时清单适配。
- 数据源命名、分组和 `DataSourceDescriptor` 注册。
- 数据库类型识别。
- PostgreSQL / MySQL / MariaDB 真实主从角色探测。
- 健康检查、健康状态注册表、状态变化事件。
- 启动安全检查和稳定违规码。
- 读库候选过滤、Load Balance、Failover / Fail-fast。
- `DataSourceRouter` 决策模型。
- 数据源定义/凭据 Port、原子刷新注册表。
- 委托 dynamic-datasource 官方上下文栈的显式 Scope 与注解适配。
- 启动摘要和定时健康监控。

不负责：

- `@DS`、`@MasterDS`、`@ReadOnlyDS` 封装。
- 自研动态路由引擎或散落的 `DynamicDataSourceContextHolder` 操作。
- Seata 集成。
- MyBatis SQL 自动读写路由拦截器。
- 应用层 master 晋升。
- 业务 Entity、Mapper、Repository、Service。
- Controller、可启动服务或管理后台。

## 3. 依赖方向

```text
dynamic-datasource
  -> synapse-datasource

synapse-data
  (no dependency on synapse-datasource)

synapse-mybatis-plus
  (no SQL routing dependency on synapse-datasource at current stage)
```

本模块可以依赖 dynamic-datasource starter 获取运行时清单，但不把 MyBatis 路由能力带入生产代码。

## 4. 核心对象

- `DatasourceInventory`：运行时数据源清单 Port。
- `DatasourceInventorySynchronizer`：同步清单、描述符和健康初始快照。
- `DataSourceDescriptorResolver` / `DataSourceDescriptorRegistry`：描述符解析和注册。
- `DataSourceValidationStrategy`：连接、数据库元数据和角色探测策略。
- `DataSourceHealthChecker` / `DataSourceHealthRegistry`：健康状态机。
- `DataSourceSafetyChecker`：启动安全检查。
- `DataSourceCandidateFilter` / `LoadBalanceSelector`：读库候选过滤和选择。
- `DataSourceFailoverManager` / `DataSourceRoutingCoordinator`：路由 fail-fast 和 fallback 决策。
- `ScheduledDataSourceHealthMonitor` / `DatasourceGovernanceLifecycle`：生命周期闭环。
- `DatasourceDefinitionProvider` / `DatasourceRegistry`：定义加载、排序和原子刷新。
- `DatasourceRouteContext` / `DatasourceRouteScope`：官方路由栈薄适配与嵌套恢复。
- `DatasourceRouteSelector`：Scope、注解、Resolver、primary 四级选择。

## 5. 主链路

```text
Spring context starts
  -> DatasourceGovernanceLifecycle.start()
  -> DatasourceInventorySynchronizer.synchronize()
  -> resolve descriptors
  -> register UNKNOWN health snapshots
  -> safety checks
  -> initial health checks if enabled
  -> startup report
  -> scheduled monitor if enabled
```

路由链路：

```text
DataSourceRouteRequest
  -> DefaultDataSourceRoutingPolicy
  -> DataSourceRoutingCoordinator
  -> candidate filter + load balance for readonly request
  -> require healthy master for write / transaction / lock / fallback
  -> DataSourceRouteDecision or DatasourceUnavailableException
```

显式选择链路：

```text
outer DatasourceRouteScope
  -> @UseDatasource
  -> ordered DatasourceRouteResolver
  -> registry primary
  -> DynamicDataSourceContextHolder.push
  -> target invocation
  -> poll and restore outer scope
```

## 6. 生命周期与失败边界

- inventory 每次巡检前都会重新同步。
- 新增数据源注册为 `UNKNOWN`，等待健康检查刷新。
- 删除数据源会同步移除 descriptor 和 health snapshot。
- 同步解析先完成再提交注册表变更，避免解析异常造成半同步。
- 定时巡检隔离单个数据源异常，也隔离整轮同步异常。
- 健康关闭时不创建调度器和定时巡检器，也不执行首轮健康检查。

## 7. 健康状态机

状态：

```text
UNKNOWN -> UP
UP -> DEGRADED -> DOWN
DOWN -> RECOVERING -> UP
RECOVERING -> DOWN
DISABLED stays unchanged
```

规则：

- UP 成功不累计 `successCount`。
- DOWN 成功进入 RECOVERING，达到 `recovery-threshold` 后变为 UP。
- `recovery-threshold=1` 时 DOWN 成功直接 UP。
- 失败达到 `failure-threshold` 后进入 DOWN。
- RECOVERING 失败直接回到 DOWN。
- 仅进入 DOWN 时发布 down 事件。
- DOWN 或 RECOVERING 恢复到 UP 时发布 recovered 事件。

## 8. 路由与 Failover 规则

- 写请求必须路由到唯一 primary master，且 master 健康状态必须为 UP。
- 事务读、写后读、锁读都强制 master，并执行同样的 UP 校验。
- 普通读请求只从健康只读候选中选择。
- 只读候选为空时，只有 `failover.enabled=true` 且 `read-fallback-to-master=true` 才允许回退到健康 master。
- master 缺失、重复或非 UP 时抛出 `DatasourceUnavailableException`。
- 路由器只返回决策，不切换数据源上下文。
- 显式 Scope/注解只通过官方 context holder 应用已确定 key；活动事务中禁止首次选择或切换。

## 9. 配置原则

有效配置只保留真实影响运行时行为的开关：

- `health.enabled`
- `safety.enabled`
- `safety.check-readonly-role`
- `safety.fail-on-master-unavailable`
- `load-balance.enabled`
- `load-balance.default-strategy`
- `load-balance.accept-degraded`
- `load-balance.accept-recovering`
- `failover.enabled`
- `failover.read-fallback-to-master`
- `failover.fail-fast-when-master-down`
- `router.enabled`

已删除伪配置：

- `load-balance.health-first`
- `failover.exclude-down-read-datasource`

## 10. 修改检查清单

- 是否新增了 `@DS` 包装或绕开统一 Scope 的切换 API。
- `DynamicDataSourceContextHolder` 是否仅存在于 RouteContext/Scope 薄适配层。
- 数据源是否在事务开始前选定，活动事务内切换是否明确失败。
- 是否引入 MyBatis SQL 自动路由。
- 是否把 DOWN master 当作可用 master 返回。
- 是否让多个 primary 时随机选择第一个。
- 是否在健康关闭时仍创建调度器或执行健康检查。
- 是否让 inventory 变更后 descriptor / health 注册表不同步。
- 是否把数据库角色探测失败误写成业务主库晋升。
