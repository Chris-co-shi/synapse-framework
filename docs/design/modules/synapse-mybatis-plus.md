# synapse-mybatis-plus 设计说明

## 1. 模块使命

`synapse-mybatis-plus` 是 MyBatis-Plus 工程增强模块。

它把 `synapse-data` 的分页、排序和审计契约适配到 MyBatis-Plus 生命周期，提供保守的默认插件链和可替换扩展点。

## 2. 边界

负责：

- MyBatis-Plus 自动配置。
- `MybatisPlusInterceptor` 默认插件链。
- 分页插件、乐观锁插件、防全表 update/delete 插件、非法 SQL 插件开关。
- `PageQuery` / `PageResult` 与 MyBatis-Plus `Page` 转换。
- `OperationContext` 到 `DataAuditorProvider` 的默认适配。
- 审计字段自动填充：`createdAt`、`updatedAt`、`createdBy`、`updatedBy`。
- `SynapseInnerInterceptorContributor` 插件链扩展点。

不负责：

- 业务 Entity、Mapper、Repository、Service。
- DataSource 配置、多数据源治理、健康检查、路由或 failover。
- MyBatis SQL 自动读写路由。
- 业务 migration、租户 SQL、数据权限。
- 初始化 `deleted`、`version` 或 `tenantId` 字段。

## 3. 依赖方向

```text
synapse-core
  <- synapse-data
       <- synapse-mybatis-plus
```

`synapse-mybatis-plus` 可以依赖 MyBatis-Plus；`synapse-data` 不能反向依赖它。

## 4. 核心对象

- `MybatisPlusPageConverter`：分页模型转换。
- `SortFieldMapping`：排序字段白名单映射。
- `OperationContextDataAuditorProvider`：从 core 上下文读取审计人。
- `SynapseMetaObjectHandler`：写入审计字段。
- `SynapseInnerInterceptorContributor`：插件链扩展 Port。
- `SynapseMybatisPlusAutoConfiguration`：默认 Bean 注册。

## 5. 主链路

```text
entry establishes OperationContext
  -> business Mapper insert/update
  -> MyBatis-Plus MetaObjectHandler
  -> DataAuditorProvider
  -> fill supported audit fields if empty
  -> SQL execution by MyBatis-Plus
```

## 6. 生命周期与失败边界

- 没有 `OperationContext` 时不填充审计人。
- `OperationActorType.UNKNOWN` 不写入审计字段。
- `OperationActorType.SYSTEM` 只有入口方显式创建时才写入。
- 插入时只填充空字段，不覆盖业务显式赋值。
- 更新时只刷新 `updatedAt` 和可用的 `updatedBy`。
- 字段类型不匹配应暴露配置问题，不吞异常伪装成功。

## 7. 扩展原则

- 排序字段必须通过白名单映射。
- 插件链扩展通过 `SynapseInnerInterceptorContributor`。
- 审计来源不同则覆盖 `DataAuditorProvider`。
- 审计字段名或规则不同则覆盖 `MetaObjectHandler`。
- 数据源治理需求进入 `synapse-datasource`，不放进本模块。

## 8. 源码阅读顺序

```text
properties
  -> page converter
  -> audit provider
  -> meta object handler
  -> interceptor contributors
  -> auto configuration
  -> tests
```

## 9. 手写练习

1. 用白名单把外部 `createdAt` 排序映射为 `created_at`。
2. 模拟 USER `OperationContext`，验证 insert 填充 created/updated 审计字段。
3. 提供一个自定义 contributor，确认插件追加顺序稳定。

## 10. 修改检查清单

- 是否新增业务 Entity、Mapper 或 migration。
- 是否绕过排序白名单。
- 是否默认写死 system。
- 是否把 DataSource、dynamic-datasource 或 SQL 路由放进本模块。
- 是否覆盖消费方显式设置的字段值。
