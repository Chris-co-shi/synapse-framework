# synapse-mybatis-plus 使用手册

## 1. 模块定位

`synapse-mybatis-plus` 是 MyBatis-Plus 工程增强模块。

它承接原 `synapse-data` 中所有 MyBatis-Plus 专属能力，`synapse-data` 不再依赖 MyBatis-Plus。

## 2. 当前事实

当前模块提供：

- MyBatis-Plus 自动配置。
- `MybatisPlusInterceptor` 默认插件链。
- `OperationContext` 到数据审计人的默认适配。
- 分页插件。
- 乐观锁插件。
- 防全表 update/delete 插件。
- 非法 SQL 插件开关。
- 审计字段自动填充。
- MyBatis-Plus ID 生成器适配。
- `PageQuery` / `PageResult` 与 MyBatis-Plus `Page` 的转换。

## 3. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-mybatis-plus</artifactId>
</dependency>
```

## 4. 配置项

```yaml
synapse:
  mybatis-plus:
    enabled: true
    pagination:
      enabled: true
      max-limit: 500
      overflow: false
    optimistic-lock:
      enabled: true
    block-attack:
      enabled: true
    illegal-sql:
      enabled: false
    audit-fill:
      enabled: true
```

## 5. 实体注解要求

业务实体属于消费方。本模块只按字段名做 MyBatis-Plus 自动填充，业务实体字段需要配置 MyBatis-Plus 注解：

```java
@TableField(fill = FieldFill.INSERT)
private Instant createdAt;

@TableField(fill = FieldFill.INSERT_UPDATE)
private Instant updatedAt;

@TableField(fill = FieldFill.INSERT)
private String createdBy;

@TableField(fill = FieldFill.INSERT_UPDATE)
private String updatedBy;

@Version
private Integer version;

@TableLogic
private Integer deleted;
```

说明：

- 插入时只填充空字段。
- 更新时刷新 `updatedAt`。
- 乐观锁字段需要 `@Version`。
- 逻辑删除字段需要 `@TableLogic` 或 MyBatis-Plus 全局逻辑删除配置。
- 当前阶段不填充 `tenantId`。

## 6. OperationContext 审计适配

默认自动配置会注册 `OperationContextDataAuditorProvider`，从 `OperationContextProvider.current()` 读取当前 `OperationActor`，用于填充 `createdBy` / `updatedBy`。

规则：

- 当前上下文不存在时不填充审计人。
- `OperationActorType.UNKNOWN` 不会写入审计字段。
- 显式 `OperationActorType.SYSTEM` 可以写入审计字段，因为 core 约定 framework 不会自动伪造 system，只有入口方显式创建时才成立。
- 不读取 `tenantId`，不实现租户字段填充。

如果业务系统需要从其他上下文读取审计人，可以提供自己的 `DataAuditorProvider` Bean 覆盖默认实现。

## 7. 边界说明

本模块不提供业务 Entity、Mapper、Repository、Service、数据库 migration 或 DataSource 配置。

数据源治理能力属于 `synapse-datasource`。SQL 自动读写路由不在当前模块中实现。
