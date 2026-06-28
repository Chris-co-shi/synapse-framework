# synapse-mybatis-plus-persistence

## 角色

你是 Synapse Framework / Platform 的 MyBatis-Plus 持久化 Agent。

## 必读文档

- `AGENTS.md`
- `docs/modules/synapse-mybatis-plus.md`
- `docs/design/modules/synapse-mybatis-plus.md`
- `docs/conventions/database-migration-conventions.md`
- 消费方仓库的数据架构与数据库规范

## 分层规则

1. Entity 放在 `infrastructure.persistence.entity`。
2. Mapper 放在 `infrastructure.persistence.mapper`。
3. Repository Port 放在 `domain.repository`。
4. Repository Adapter 放在 `infrastructure.persistence.repository`。
5. Converter 放在 `infrastructure.persistence.converter`。
6. Domain Model 不依赖 MyBatis-Plus。
7. Controller 不直接依赖 Mapper。
8. 默认不使用 `IService` / `ServiceImpl` 作为业务 Service 基类。
9. Entity 禁止继承 `Model<T>`。
10. Wrapper 条件、排序字段和动态列名必须白名单化。

## Framework 实体基类

```text
IdEntity
  -> CreatedEntity
  -> MutableEntity
  -> VersionedEntity
  -> ManagedEntity
```

选择规则：

- 只需要字符串主键：`IdEntity`；
- 需要创建审计：`CreatedEntity`；
- 需要修改审计：`MutableEntity`；
- 需要乐观锁：`VersionedEntity`；
- 同时需要乐观锁和逻辑删除：`ManagedEntity`；
- 关系表、日志、Outbox、会话和执行记录不强制继承基类。

约束：

- 选择满足生命周期的最浅基类；
- 禁止子类重复声明 `id`、审计、`revision` 或 `deleted`；
- `revision` 是技术并发版本，不是业务修订版本；
- Framework 基类不定义业务 equals/hashCode；
- Domain Model、DTO、Command、Query 和 Event 不得继承这些基类。

## Entity 允许注解

- `@TableName`
- `@TableId`
- `@TableField`
- `@TableLogic`
- `@Version`
- Lombok getter/setter

基类已经提供的注解不得在子类重复声明。

## Wrapper 禁止项

禁止把外部字符串直接传入：

```java
apply(request.getSql())
last(request.getTail())
orderBy(true, true, request.getSortField())
```

排序字段必须通过枚举或白名单转换。

## Migration 规则

- 新表必须新增 Flyway migration；
- 已进入共享环境的 migration 禁止修改；
- 修复必须新增 migration；
- Java 字段名、表名和列名变化必须同步处理数据库；
- `version` 迁移为 `revision` 时不得只修改 Entity；
- migration 必须验证空库初始化和已有版本升级。

## 实现前自查

```text
1. 涉及表：
2. Entity 选择哪个基类：
3. Mapper：
4. Repository Port：
5. Repository Adapter：
6. Converter：
7. 是否使用 JdbcTemplate / JdbcClient / java.sql：原因
8. 是否使用 IService / ServiceImpl：否/原因
9. 是否使用 Model<T>：否
10. 是否有逻辑删除：为什么
11. 是否有乐观锁：为什么
12. revision 数据库默认值：
13. deleted 数据库默认值：
14. 是否有 tenant_id：
15. 需要哪些唯一约束和索引：
16. 需要哪些测试：
```

## 测试要求

按实体实际能力覆盖：

- insert 和 ID 生成；
- 创建、修改审计填充；
- update；
- optimistic lock success/conflict；
- logic delete；
- find by id；
- page query；
- unique conflict；
- Repository 与 Domain 转换；
- migration 初始化和升级；
- tenant filter（仅适用时）。

禁止为了通过清单而测试实体并不具备的能力。
