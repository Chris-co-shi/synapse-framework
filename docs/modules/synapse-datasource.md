# synapse-datasource 使用手册

## 1. 模块定位

`synapse-datasource` 是数据源治理模块，不是 ORM 模块。

它覆盖多数据源运行时清单适配、数据源命名和分组规范、数据源描述符注册表、数据库类型识别、数据库真实角色探测、连接安全检测、健康检查、健康状态注册表、故障数据源摘除、故障恢复检测、读库 Load Balance、Router 抽象、Failover / Fail-fast 抽象、启动诊断和运行时状态查询基础模型。

## 2. 底层依赖

底层使用 baomidou `dynamic-datasource-spring-boot3-starter` 作为多数据源引擎。

## 3. 数据源命名约定

推荐配置：

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: true
      datasource:
        master:
          url: jdbc:postgresql://db-vip:5432/synapse_iam
          username: postgres
          password: postgres
          driver-class-name: org.postgresql.Driver

        slave_1:
          url: jdbc:postgresql://db-slave-1:5432/synapse_iam
          username: postgres
          password: postgres
          driver-class-name: org.postgresql.Driver

        slave_2:
          url: jdbc:postgresql://db-slave-2:5432/synapse_iam
          username: postgres
          password: postgres
          driver-class-name: org.postgresql.Driver
```

约定：

```text
master       = 写库 / 主库 / 默认数据源
slave_*      = 读库组
report_*     = 报表库组
archive_*    = 归档库组
external_*   = 外部只读库组
```

## 4. Synapse 治理配置

```yaml
synapse:
  datasource:
    enabled: true

    convention:
      require-strict: true
      master-name: master
      slave-group: slave
      report-group: report
      archive-group: archive
      external-group: external

    detection:
      enabled: true
      fail-on-unknown: false
      prefer-explicit: true
      explicit-types:
        master: POSTGRESQL

    health:
      enabled: true
      initial-delay: 10s
      interval: 30s
      timeout: 2s
      failure-threshold: 3
      recovery-threshold: 2

    safety:
      enabled: true
      check-on-startup: true
      check-readonly-role: false
      fail-on-master-unavailable: true

    load-balance:
      enabled: true
      default-strategy: ROUND_ROBIN
      accept-degraded: false
      accept-recovering: false

    failover:
      enabled: true
      read-fallback-to-master: true
      fail-fast-when-master-down: true

    router:
      enabled: false
      sql-auto-routing: false
      force-master-in-transaction: true
      force-master-after-write: true
      force-master-for-lock-query: true
```

## 5. 当前阶段明确不支持

```text
当前不提供 `@DS` 封装。
当前不提供 `@MasterDS`。
当前不提供 `@ReadOnlyDS`。
当前不提供业务显式切换数据源 API。
当前不提供 Seata 集成。
当前不提供 MyBatis SQL 自动读写路由拦截器。
当前不做应用层 master 晋升。
```

## 6. 后续扩展方向

后续 SQL 自动路由应独立设计，例如：

```text
synapse-mybatis-datasource-router
```

当前 `synapse-datasource` 只提供 Router 领域模型和数据源治理能力，不绑定 MyBatis，不解析 SQL，不操作 `DynamicDataSourceContextHolder`。

## 7. 运行时治理闭环

自动配置在存在 dynamic-datasource 运行时或消费方自定义 `DatasourceInventory` 时启用治理生命周期：

1. 读取运行时数据源清单。
2. 解析并注册 `DataSourceDescriptor`。
3. 为每个数据源登记初始 `UNKNOWN` 健康快照。
4. 执行 primary 名称和 strict 模式安全检查。
5. 执行首轮健康检查。
6. 输出脱敏启动摘要。
7. 启动定时健康监控。

框架健康监控固定使用名为 `synapseDatasourceTaskScheduler` 的调度器。应用存在其他
`TaskScheduler` Bean 时不会产生注入歧义；消费方需要替换框架调度器时应使用同一 Bean 名。

运行期定时巡检每轮都会重新同步 inventory。新增数据源会注册描述符和初始 `UNKNOWN` 健康快照；删除数据源会从描述符注册表和健康注册表中移除。

数据库类型识别顺序为：显式配置、JDBC URL、连接 metadata、`UNKNOWN`。

## 8. 健康状态机

健康状态包括：

```text
UNKNOWN
UP
DEGRADED
DOWN
RECOVERING
DISABLED
```

核心迁移规则：

- 首次成功检查：`UNKNOWN -> UP`。
- 失败达到 `failure-threshold`：进入 `DOWN`。
- `DOWN` 成功后进入 `RECOVERING`，连续成功达到 `recovery-threshold` 后进入 `UP`。
- `recovery-threshold=1` 时，`DOWN` 成功直接进入 `UP`。
- `RECOVERING` 期间失败会回到 `DOWN`。
- `DISABLED` 不参与自动迁移。

PostgreSQL 使用 `pg_is_in_recovery()` 探测真实角色；MySQL / MariaDB 使用 `@@read_only` 和 `@@super_read_only` 探测真实角色。Oracle 当前只做连通性检查，角色探测保留为扩展点。

## 9. 路由说明

`router.enabled=false` 时不注册 `DataSourceRouter`。

`router.enabled=true` 时注册 `DataSourceRoutingCoordinator`，它只生成 `DataSourceRouteDecision`，不会切换 dynamic-datasource 上下文，也不会拦截 SQL。

路由规则：

- 写请求、事务读、写后读和锁读必须使用唯一 primary master。
- master 健康状态不是 `UP` 时抛出 `DatasourceUnavailableException`。
- 普通读请求只从健康只读候选中选择。
- 读候选为空时，只有 `failover.enabled=true` 且 `read-fallback-to-master=true` 才允许回退到健康 master。
- master 缺失或存在多个 primary 时 fail-fast。

## 10. 已删除配置项

以下旧配置已删除，因为它们没有真实控制运行时行为：

```text
synapse.datasource.load-balance.health-first
synapse.datasource.failover.exclude-down-read-datasource
```
