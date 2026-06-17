# synapse-data Skill

## 职责

`synapse-data` 只提供数据层技术支撑、MyBatis-Plus 配置、技术型基础模型和 OperationContext 自动填充。

## 禁止事项

- 不新增业务 Entity、Mapper、Repository、Service。
- 不新增业务表结构或业务 migration。
- 不把 Entity 暴露为 API 返回对象。
- 不新增 Controller 或可启动服务。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 只允许技术型 `BaseEntity`、`AuditableEntity`、`VersionedEntity`。
- Mapper / Entity 只能用于 framework 技术测试或技术基础设施。
- 多数据源和事务能力必须保持技术抽象边界。

## 验证

- 搜索 `@TableName`、`BaseMapper`、`IService`、`ServiceImpl`、`CREATE TABLE`。
- 确认命中不是业务模型。
