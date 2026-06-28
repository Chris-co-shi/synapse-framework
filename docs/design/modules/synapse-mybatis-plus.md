# synapse-mybatis-plus 设计说明

## 1. 模块使命

`synapse-mybatis-plus` 把 `synapse-data` 的分页、排序和审计契约适配到 MyBatis-Plus 生命周期，并提供保守的插件链、扩展点和持久化实体技术基类。

## 2. 边界

负责：

- MyBatis-Plus 自动配置；
- 默认插件链；
- 分页、乐观锁、防全表 update/delete 和非法 SQL 插件；
- 分页模型转换和安全排序映射；
- `OperationContext` 到 `DataAuditorProvider` 的适配；
- 审计字段填充；
- MyBatis-Plus ID 生成适配；
- MyBatis-Plus 持久化实体的分层技术基类；
- `SynapseInnerInterceptorContributor` 扩展点。

不负责：

- 具体业务 Entity、Mapper、Repository 和 Service；
- 业务 migration；
- DataSource 治理和 SQL 路由；
- 租户 SQL、数据权限；
- 初始化 `revision`、`deleted` 或 `tenantId`；
- 业务领域中的版本、删除和相等性语义。

## 3. 依赖方向

```text
synapse-core
  <- synapse-data
       <- synapse-mybatis-plus
```

`synapse-data` 不依赖 MyBatis-Plus。带 MyBatis-Plus 注解的实体基类只存在于本模块。

## 4. 实体基类设计

```text
IdEntity
  └── CreatedEntity
       └── MutableEntity
            └── VersionedEntity
                 └── ManagedEntity
```

### IdEntity

- `String id`；
- `@TableId(type = ASSIGN_ID)`；
- 只表达持久化技术身份。

### CreatedEntity

- `Instant createdAt`；
- `String createdBy`；
- 两者在 INSERT 阶段填充。

### MutableEntity

- `Instant updatedAt`；
- `String updatedBy`；
- 两者在 INSERT_UPDATE 阶段填充。

### VersionedEntity

- `Integer revision`；
- 使用 `@Version`；
- 只表达技术并发控制版本。

### ManagedEntity

- `Integer deleted`；
- 使用 `@TableLogic(value = "0", delval = "1")`；
- 同时继承乐观锁能力。

实体应选择满足生命周期的最浅基类，Framework 不要求所有表继承完整层级。

## 5. 相等性策略

Framework 实体基类不生成或覆盖 `equals/hashCode`。

原因：

- 未持久化实体的 ID 可能为空；
- 不同业务对自然键、代理键和对象身份的判断不同；
- 把 `revision`、`deleted` 或审计字段加入相等性会导致集合行为不稳定；
- 局部子类生成 `equals/hashCode(callSuper = true)` 无法形成一致的继承语义。

具体实体若需要值相等或实体身份比较，由消费方按照领域规则实现。

## 6. 核心对象

- `IdEntity` / `CreatedEntity` / `MutableEntity` / `VersionedEntity` / `ManagedEntity`；
- `MybatisPlusPageConverter`；
- `SortFieldMapping`；
- `OperationContextDataAuditorProvider`；
- `SynapseMetaObjectHandler`；
- `SynapseInnerInterceptorContributor`；
- `SynapseMybatisPlusIdentifierGenerator`；
- `SynapseMybatisPlusAutoConfiguration`。

## 7. 审计链路

```text
entry establishes OperationContext
  -> Mapper insert/update
  -> MyBatis-Plus MetaObjectHandler
  -> DataAuditorProvider
  -> fill supported audit fields
  -> SQL execution
```

- 没有 `OperationContext` 时不填充审计人；
- `UNKNOWN` actor 不写入；
- `SYSTEM` 必须由入口方显式创建；
- 插入不覆盖业务显式赋值；
- 更新刷新 `updatedAt` 和可用的 `updatedBy`；
- 字段类型不匹配时暴露错误。

## 8. 乐观锁和逻辑删除

Framework 提供字段和注解，但不决定某个业务实体是否应该采用它们。

- 采用 `VersionedEntity` 或 `ManagedEntity` 时，数据库需要提供 `revision` 初始值；
- 采用 `ManagedEntity` 时，数据库需要提供 `deleted` 初始值；
- 乐观锁冲突如何映射为业务错误，由消费方决定；
- Refresh Token rotation 等安全状态机不能只依赖普通 `@Version`；
- 日志、Outbox 和关系表通常不应为了复用而继承 `ManagedEntity`。

## 9. 扩展原则

- 排序字段必须通过白名单；
- 插件链通过 `SynapseInnerInterceptorContributor` 扩展；
- 审计来源不同则覆盖 `DataAuditorProvider`；
- 审计字段规则不同则覆盖 `MetaObjectHandler`；
- DataSource 治理进入 `synapse-datasource`；
- 业务持久化模型留在消费方。

## 10. 测试基线

Framework 至少验证：

- 继承关系稳定；
- `String id` 与 `ASSIGN_ID` 注解；
- 审计字段和填充策略；
- `revision` 与 `@Version`；
- `deleted` 与 `@TableLogic`；
- 基类不生成自定义 `equals/hashCode`；
- 自动配置中的乐观锁和其他默认插件。

真实数据库 insert、update、冲突和逻辑删除行为，由 Framework 集成测试或消费方 PostgreSQL 测试进一步覆盖。

## 11. 修改检查清单

- 是否把业务 Entity 或 migration 放入 Framework；
- 是否让 `synapse-data` 反向依赖 MyBatis-Plus；
- 是否改变标准字段名而未更新 `DataFieldNames`；
- 是否在基类中引入业务相等性；
- 是否默认初始化不同业务可能采用不同类型的字段；
- 是否绕过排序白名单；
- 是否默认伪造 SYSTEM actor。
