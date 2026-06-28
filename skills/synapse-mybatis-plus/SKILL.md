# synapse-mybatis-plus Skill

## 职责

`synapse-mybatis-plus` 提供 MyBatis-Plus 工程增强和持久化实体技术基类。

## 允许内容

- MyBatis-Plus 自动配置和默认插件链；
- 分页、乐观锁、防全表更新删除、非法 SQL 插件；
- 安全排序字段白名单；
- 创建、修改审计字段自动填充；
- OperationContext 审计适配；
- ID 生成和分页模型转换；
- 以下实体技术基类：

```text
IdEntity
  -> CreatedEntity
  -> MutableEntity
  -> VersionedEntity
  -> ManagedEntity
```

## 实体规则

- Entity 按真实生命周期选择满足需求的最浅基类；
- Domain Model、DTO 和事件契约不得继承；
- 不要求所有 Entity 继承 `ManagedEntity`；
- 特殊关系表、日志、Outbox、会话和执行记录可以不继承；
- `revision` 只表达技术乐观锁；
- `deleted` 只表达逻辑删除；
- Framework 不初始化 `revision` 和 `deleted`；
- Framework 基类不定义业务相等性；
- 子类禁止重复声明基类字段。

## 禁止事项

- 不新增具体业务 Entity、Mapper、Repository、Service 或 migration；
- 不做 DataSource 治理和 SQL 自动读写路由；
- 不引入分布式事务；
- 不新增 Controller 或启动服务；
- 不让 `synapse-data` 反向依赖 MyBatis-Plus；
- 不把所有实体机械套入完整继承层级。

## 验证

- 继承关系与字段名保持稳定；
- ID 使用 String 和 ASSIGN_ID；
- 审计字段填充策略正确；
- `revision` 使用乐观锁注解；
- `deleted` 使用逻辑删除注解；
- 基类不生成自定义 equals/hashCode；
- 文档、字段常量和源码一致。
