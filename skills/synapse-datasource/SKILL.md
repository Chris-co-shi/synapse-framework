# synapse-datasource Skill

## 职责

提供数据源发现、描述、类型识别、健康检查、角色探测、候选选择和故障决策能力。

实际数据源创建与切换由 baomidou dynamic-datasource 官方实现负责。

## 允许内容

- dynamic-datasource 运行时清单适配。
- 数据源描述符和数据库类型识别。
- 数据库真实角色探测。
- 健康状态机、事件和定时巡检。
- 读库候选过滤、负载均衡和故障决策。
- `DataSourceRouter` 领域决策模型。

## 禁止事项

- 不包装官方 `@DS`。
- 不新增 `@UseDatasource`、`@MasterDS` 或 `@ReadOnlyDS`。
- 不新增 RouteContext、RouteScope、RouteSelector 或 RouteResolver。
- 不新增运行时 DatasourceDefinition Registry。
- 不自行注册 AOP AutoProxyCreator。
- 不操作 dynamic-datasource 上下文栈。
- 不实现 MyBatis SQL 自动读写路由。
- 不集成 Seata。
- 不新增业务 Entity、Mapper、Repository、Service 或可启动服务。

## 验证

- 生产代码不得引用 `DynamicDataSourceContextHolder`。
- 仓库不得存在 `UseDatasource` 类型。
- `router.enabled=false` 时不得注册 `DataSourceRouter`。
- `DataSourceRouter` 只能产生决策，不得执行实际切换。
