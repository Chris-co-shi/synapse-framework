# 数据模块边界迁移指南

## 1. 迁移背景

数据能力拆分为：

- `synapse-data`：ORM 无关的数据语义抽象；
- `synapse-mybatis-plus`：MyBatis-Plus 工程增强和持久化实体技术基类；
- `synapse-datasource`：数据源治理。

旧 `synapse-data` 不再承载 MyBatis-Plus、dynamic-datasource、Flyway、旧版 `BaseEntity` 或 JSON TypeHandler。

## 2. 依赖迁移

只使用分页、排序、字段名常量时保留：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-data</artifactId>
</dependency>
```

使用 MyBatis-Plus 插件、自动填充、ID 生成、分页转换或实体基类时引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-mybatis-plus</artifactId>
</dependency>
```

使用多数据源治理、数据库类型识别、健康检查或路由决策时引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-datasource</artifactId>
</dependency>
```

## 3. 类型迁移表

| 旧类型 / 能力 | 新位置 / 处理方式 |
| --- | --- |
| `SynapseDataAutoConfiguration` | `SynapseMybatisPlusAutoConfiguration` |
| `SynapseIdentifierGenerator` | `SynapseMybatisPlusIdentifierGenerator` |
| `SynapseMetaObjectHandler` | `com.indigo.synapse.mybatisplus.fill.SynapseMetaObjectHandler` |
| `SynapseAuditorProvider` | `synapse-data` 的 `DataAuditorProvider` |
| 旧 `BaseEntity` / `BaseLogEntity` | 不提供兼容同名类型；根据生命周期迁移到新实体基类或自行定义特殊模型 |
| `SynapseDataSource` / `DataSourceContext` | 当前不提供 `@DS` 或显式切换 API |
| `DatabaseType` / `DatabaseDialect` | `SynapseDbType` 和数据库检测器 |
| `JsonValueTypeHandler` | 消费方按 ORM 和数据库自行实现 |
| Flyway migration 测试能力 | 不属于 Framework data 模块 |

## 4. 新实体基类

`synapse-mybatis-plus` 提供：

```text
IdEntity
  └── CreatedEntity
       └── MutableEntity
            └── VersionedEntity
                 └── ManagedEntity
```

| Base | Fields |
| --- | --- |
| `IdEntity` | `String id` + `ASSIGN_ID` |
| `CreatedEntity` | `createdAt`, `createdBy` |
| `MutableEntity` | `updatedAt`, `updatedBy` |
| `VersionedEntity` | `Integer revision` + `@Version` |
| `ManagedEntity` | `Integer deleted` + `@TableLogic` |

迁移时必须选择满足真实生命周期的最浅基类：

```java
@TableName("user_account")
public class UserEntity extends VersionedEntity {
    private String username;
}
```

禁止：

- Domain Model、DTO 或 Event 继承这些类型；
- 所有实体机械继承 `ManagedEntity`；
- 为关系表、日志、Outbox 或会话强行增加无意义字段；
- 把技术字段 `revision` 当成业务修订版本；
- 同时在子类重复声明基类字段。

## 5. 审计字段填充

默认 `OperationContextDataAuditorProvider` 从 `OperationContextProvider` 读取 actor id，用于填充：

- `createdBy`；
- `updatedBy`。

实体基类已经声明：

- `createdAt` / `createdBy` 使用 `FieldFill.INSERT`；
- `updatedAt` / `updatedBy` 使用 `FieldFill.INSERT_UPDATE`。

规则：

- 没有上下文时不填充审计人；
- `UNKNOWN` actor 不填充；
- `SYSTEM` 必须由入口方显式创建；
- Framework 不填充 `tenantId`；
- Framework 不初始化 `revision` 和 `deleted`。

## 6. 字段名变化

Framework 标准乐观锁字段为：

```text
revision
```

`DataFieldNames.REVISION` 是新标准常量。

`DataFieldNames.VERSION` 保留旧值 `version` 并标记为废弃，用于避免现有消费方源码立即破坏。新代码不得继续使用它作为 Framework 基类字段。

## 7. 下游扫描建议

```bash
rg -n "com\.indigo\.synapse\.data\.entity|BaseEntity|BaseLogEntity|SynapseMetaObjectHandler|SynapseDataSource|JsonValueTypeHandler|SynapseIdentifierGenerator|SynapseDataAutoConfiguration|DataFieldNames\.VERSION|private .* version" .
```

逐项判断：

- 是否可以继承新的最浅基类；
- 是否属于不应继承基类的特殊表；
- 数据库字段应继续为 `version`，还是通过正式 migration 调整为 `revision`；
- 子类是否重复声明 ID、审计、乐观锁或逻辑删除字段。

禁止只修改 Java 字段或注解而不处理已有数据库结构。

## 8. 当前不提供的能力

- `@DS` 封装；
- `@MasterDS`；
- `@ReadOnlyDS`；
- 业务显式切换数据源 API；
- MyBatis SQL 自动读写路由；
- 应用层主库晋升；
- 租户字段自动填充；
- 业务数据库 migration；
- 业务实体相等性策略。
