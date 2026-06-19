# 数据模块边界迁移指南

## 1. 迁移背景

数据相关能力已拆分为三个模块：

- `synapse-data`：ORM 无关的数据语义抽象。
- `synapse-mybatis-plus`：MyBatis-Plus 工程增强。
- `synapse-datasource`：数据源治理。

旧 `synapse-data` 不再承载 MyBatis-Plus、dynamic-datasource、Flyway、BaseEntity 或 JSON TypeHandler。

## 2. 依赖迁移

旧依赖：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-data</artifactId>
</dependency>
```

如果只使用分页、排序、字段名常量，保留 `synapse-data`。

如果使用 MyBatis-Plus 插件、自动填充、ID 生成器或分页转换，新增：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-mybatis-plus</artifactId>
</dependency>
```

如果使用多数据源治理、数据库类型识别、健康检查、负载均衡或路由决策，新增：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-datasource</artifactId>
</dependency>
```

## 3. 类型迁移表

| 旧类型 / 能力 | 新位置 / 处理方式 |
| --- | --- |
| `SynapseDataAutoConfiguration` | `synapse-mybatis-plus` 的 `SynapseMybatisPlusAutoConfiguration` |
| `SynapseIdentifierGenerator` | `SynapseMybatisPlusIdentifierGenerator` |
| `SynapseMetaObjectHandler` | `com.indigo.synapse.mybatisplus.fill.SynapseMetaObjectHandler` |
| `SynapseAuditorProvider` | `synapse-data` 的 `DataAuditorProvider` |
| `BaseEntity` / `BaseLogEntity` | 不再提供；业务系统自行定义实体基类或字段 |
| `SynapseDataSource` / `DataSourceContext` | 不再提供；当前 `synapse-datasource` 不封装 `@DS` 或显式切换 API |
| `DatabaseType` / `DatabaseDialect` | `synapse-datasource` 的 `SynapseDbType` 和检测器 |
| `JsonValueTypeHandler` | 不再提供；消费方按具体 ORM/数据库自行实现 |
| Flyway migration 测试能力 | 不再属于 framework data 模块 |

## 4. 审计字段填充

`synapse-mybatis-plus` 默认提供 `OperationContextDataAuditorProvider`，会从 `OperationContextProvider` 读取当前 actor id，用于填充：

- `createdBy`
- `updatedBy`

当前不会填充：

- `tenantId`

审计人解析规则：

- 没有 `OperationContext` 时不填充。
- `OperationActorType.UNKNOWN` 不填充。
- 显式 `OperationActorType.SYSTEM` 可以填充，例如入口方明确创建的系统任务上下文。

业务实体仍需要按 MyBatis-Plus 要求标注字段填充规则：

```java
@TableField(fill = FieldFill.INSERT)
private String createdBy;

@TableField(fill = FieldFill.INSERT_UPDATE)
private String updatedBy;
```

## 5. 下游扫描建议

升级前建议在消费方仓库执行：

```bash
rg -n "com\\.indigo\\.synapse\\.data\\.entity|SynapseMetaObjectHandler|SynapseDataSource|JsonValueTypeHandler|SynapseIdentifierGenerator|SynapseDataAutoConfiguration" .
```

命中后按上方迁移表处理。

## 6. 当前明确不迁移的能力

以下能力当前不提供兼容替代：

- `@DS` 封装。
- `@MasterDS`。
- `@ReadOnlyDS`。
- 业务显式切换数据源 API。
- MyBatis SQL 自动读写路由拦截器。
- 应用层主库晋升。
- 租户字段自动填充。
