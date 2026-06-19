# synapse-datasource Skill

## 职责

`synapse-datasource` 提供数据源治理能力，不是 ORM 模块。

## 允许内容

- dynamic-datasource 基础接入。
- 数据源命名和分组规范。
- 数据源运行时清单适配。
- 数据源描述模型、描述符注册表、数据库类型识别、角色识别。
- 连接安全检测。
- 健康检查和健康状态注册表。
- 故障摘除和恢复检测。
- 读库 Load Balance。
- Router 抽象和基础路由决策。
- Failover / Failback 抽象。
- 启动诊断和运行时状态查询基础模型。
- 生命周期闭环：发现清单、注册描述符、初始健康快照、安全检查、首轮健康检查、启动摘要、定时健康监控。

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
