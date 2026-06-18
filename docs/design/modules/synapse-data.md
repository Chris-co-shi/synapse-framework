# synapse-data 设计说明

## 1. 模块使命

`synapse-data` 把 core `OperationContext` 适配到 MyBatis-Plus 数据写入生命周期，提供分页、乐观锁、ID 默认实现和通用审计字段自动填充，但不拥有任何业务实体或数据库结构。

## 2. 边界

负责：

- MyBatis-Plus 分页与乐观锁插件默认配置。
- `IdentifierGenerator` 默认实现。
- `SynapseAuditorProvider`。
- `SynapseMetaObjectHandler`。
- 从 OperationContext 读取 actor / tenantId 承载位。

不负责：

- DataSource、连接池和事务业务边界。
- 业务 Entity、Mapper、Repository、SQL、migration。
- DataScope、租户 SQL 自动拼接。
- 审计日志表和业务审计事件。

## 3. 为什么 data 不依赖 security

数据写入可能来自 HTTP、MQ、Task、Async 或补偿流程。直接读取 LoginUser 会绑定 Web 登录场景，正确依赖是：

```text
security / mq / task entry
  -> OperationContext
  -> data
```

没有上下文时不默认填 `system`，否则真实来源无法追溯。

## 4. 核心对象角色

### 4.1 `SynapseAuditorProvider`

把通用 OperationContext 转换为数据字段需要的 `currentAuditor` 和 `currentTenantId`。该 Port 便于消费方覆盖特殊审计来源。

### 4.2 `SynapseMetaObjectHandler`

在 MyBatis-Plus insert/update 生命周期中按字段名约定填充：

- insert：createdAt、updatedAt、createdBy、updatedBy、deleted、version、tenantId。
- update：updatedAt、存在 actor 时更新 updatedBy。

插入只填空值，不覆盖业务代码显式赋值。

### 4.3 `MybatisPlusInterceptor`

默认使用 `DbType.OTHER`，避免 Framework 锁定 MySQL/PostgreSQL。消费方可提供数据库明确版本。

### 4.4 `SynapseIdentifierGenerator`

通用技术默认 ID，不用于业务单号。数据库序列、发号服务和业务编码由消费方替换。

## 5. 主链路

```text
entry establishes OperationContext
  -> business Mapper insert/update
  -> MyBatis-Plus MetaObjectHandler
  -> SynapseAuditorProvider
  -> OperationContextProvider
  -> fill audit fields if present
  -> SQL execution
```

## 6. 生命周期与失败边界

- 没有 actor：时间、deleted、version 等仍可填；createdBy/updatedBy 保持未填，而不是写死 system。
- 业务显式设置 createdBy：insert fill 不覆盖。
- update 不应修改 createdAt / createdBy。
- 字段类型不匹配应明确暴露配置问题。
- tenantId 只是承载位，不等于已经实现租户隔离。

## 7. 扩展原则

- 数据库类型：覆盖 `MybatisPlusInterceptor`。
- ID 策略：覆盖 `IdentifierGenerator`。
- 字段名或规则不同：覆盖 `MetaObjectHandler`。
- 审计来源不同：覆盖 `SynapseAuditorProvider` 或 OperationContextProvider。
- 不通过增加 BaseEntity 强迫所有消费方继承。

## 8. 源码阅读顺序

```text
SynapseAuditorProvider
  -> default OperationContext-based provider
  -> SynapseMetaObjectHandler
  -> SynapseIdentifierGenerator
  -> SynapseDataAutoConfiguration
  -> insert/update integration tests
```

## 9. 手写练习

1. 创建含标准字段的测试 Entity。
2. 有 USER OperationContext 时 insert，验证 createdBy/updatedBy。
3. 无上下文时 insert，验证不写 system。
4. 显式设置 createdBy，验证 handler 不覆盖。
5. update 时只更新 updated 字段。

## 10. 修改检查清单

- 是否直接依赖 SecurityContext / LoginUser。
- 是否默认写死 system。
- 是否把业务 Entity、Mapper 或 migration 放入模块。
- 是否把 tenantId 承载误写成租户隔离能力。
- 是否覆盖消费方显式字段值。
- 是否绑定具体数据库类型。
- 用户自定义 MyBatis Bean 是否能让默认配置退让。
