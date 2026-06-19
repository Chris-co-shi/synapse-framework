# synapse-datasource Skill

## 职责

`synapse-datasource` 提供数据源治理能力，不是 ORM 模块。

## 允许内容

- dynamic-datasource 基础接入。
- 数据源命名和分组规范。
- 数据源运行时清单适配。
- 数据源描述模型、描述符注册表、数据库类型识别、角色识别。
- 连接安全检测。
- 健康检查、健康状态注册表和状态变化事件。
- PostgreSQL / MySQL / MariaDB 真实数据库角色探测。
- 故障摘除和恢复检测。
- 读库 Load Balance。
- Router 抽象和基础路由决策。
- Failover / Fail-fast 抽象。
- 启动诊断和运行时状态查询基础模型。
- 生命周期闭环：发现清单、注册描述符、初始健康快照、安全检查、首轮健康检查、启动摘要、定时健康监控、运行期 inventory 同步。

## 运行时规则

- 新增数据源先注册为 `UNKNOWN`，等待健康检查刷新。
- 删除数据源必须同步移除 descriptor 和 health snapshot。
- 写请求、事务读、写后读和锁读必须使用唯一且健康状态为 `UP` 的 primary master。
- 普通读请求没有可用只读候选时，只有 failover 开启且允许 read fallback 时才回退到健康 master。
- master 缺失、重复或非 `UP` 时必须 fail-fast。
- 健康关闭时不创建定时巡检器，不执行首轮健康检查。
- 健康监控必须限定注入 `synapseDatasourceTaskScheduler`，不得因应用存在多个 `TaskScheduler` 而歧义。
- 用户提供 `ScheduledDataSourceHealthMonitor` 时默认实现必须退让。

## 禁止事项

- 不封装 `@DS`。
- 不新增 `@MasterDS`。
- 不新增 `@ReadOnlyDS`。
- 不提供业务显式切换数据源 API。
- 不集成 Seata。
- 不实现 MyBatis SQL 自动读写路由拦截器。
- 不做应用层 master 晋升。
- 不新增业务 Entity、Mapper、Repository、Service。
- 不新增 Controller 或可启动服务。

## 验证

- 搜索 `DynamicDataSourceContextHolder`，当前阶段不应在生产代码中操作它。
- 搜索 MyBatis 拦截器类型，当前模块不应提供 SQL 自动路由拦截器。
- 确认 `router.enabled=false` 时不注册 `DataSourceRouter`。
- 确认 `load-balance.health-first` 和 `failover.exclude-down-read-datasource` 不再作为配置项出现。
