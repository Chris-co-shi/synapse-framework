# synapse-data 使用手册

## 1. 模块定位

`synapse-data` 是 Synapse Framework 的数据层基础设施模块。

它提供 MyBatis-Plus 基础配置和基于 `OperationContext` 的通用字段自动填充能力，不提供业务 Entity、Mapper、Repository 或数据库连接配置。

当前核心能力：

- MyBatis-Plus 分页插件。
- MyBatis-Plus 乐观锁插件。
- 默认 ID 生成器。
- `OperationContextProvider` 默认读取实现。
- `SynapseAuditorProvider` 审计字段读取端口。
- `SynapseMetaObjectHandler` 自动字段填充。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-data`：

- 使用 MyBatis-Plus。
- 需要统一分页和乐观锁插件。
- 需要统一 ID 生成策略默认值。
- 需要自动填充 `createdAt`、`updatedAt`、`createdBy`、`updatedBy` 等通用字段。
- 需要从 core `OperationContext` 读取当前操作人填充审计字段。
- 需要在不依赖 security / web 的情况下完成数据层审计字段填充。

## 3. 不适用场景

`synapse-data` 不适合承担以下职责：

- 业务 Entity。
- 业务 Mapper。
- 业务 Repository。
- 数据库 migration。
- 数据库连接池配置。
- 业务 SQL。
- 业务查询封装。
- DataScope。
- 业务审计落库。

这些能力应由业务系统或平台服务实现。

## 4. Maven 引入

推荐先引入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

再引入 data 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-data</artifactId>
</dependency>
```

业务系统仍需自行配置：

- DataSource。
- Mapper 扫描。
- 业务 Entity。
- 业务 Mapper XML 或注解 SQL。
- migration。

## 5. 核心能力

### 5.1 自动配置

核心类型：

```java
SynapseDataAutoConfiguration
```

默认注册：

```java
MybatisPlusInterceptor
Clock
IdentifierGenerator
OperationContextProvider
SynapseAuditorProvider
SynapseMetaObjectHandler
```

所有 Bean 均支持消费方自定义覆盖。

### 5.2 MyBatis-Plus 插件

默认注册：

```java
PaginationInnerInterceptor(DbType.OTHER)
OptimisticLockerInnerInterceptor
```

说明：

- `DbType.OTHER` 避免 framework 绑定具体数据库。
- 如果业务系统需要指定 PostgreSQL、MySQL 等数据库类型，可以提供自己的 `MybatisPlusInterceptor` Bean。

### 5.3 ID 生成器

核心类型：

```java
SynapseIdentifierGenerator
```

它委托 MyBatis-Plus `DefaultIdentifierGenerator`。

如果业务系统需要：

- 数据库序列。
- 分布式发号服务。
- 业务单号。
- 自定义雪花节点。

应提供自己的 `IdentifierGenerator` Bean。

### 5.4 自动字段填充

核心类型：

```java
SynapseMetaObjectHandler
SynapseAuditorProvider
```

默认约定字段名：

| 字段 | 插入填充 | 更新填充 | 说明 |
| --- | --- | --- | --- |
| `createdAt` | 是 | 否 | 创建时间 |
| `updatedAt` | 是 | 是 | 更新时间 |
| `createdBy` | 是 | 否 | 创建人 |
| `updatedBy` | 是 | 是 | 更新人 |
| `deleted` | 是 | 否 | 逻辑删除标记，默认 0 |
| `version` | 是 | 否 | 乐观锁版本，默认 0 |
| `tenantId` | 是 | 否 | 租户标识，当前只保留承载位 |

插入填充时只填充空字段，不覆盖业务侧显式赋值。

更新填充时会刷新 `updatedAt`，如果存在当前操作人，则刷新 `updatedBy`。

### 5.5 OperationContext 接入

默认链路：

```text
OperationContextHolder
  -> DefaultOperationContextProvider
  -> SynapseAuditorProvider
  -> SynapseMetaObjectHandler
  -> createdBy / updatedBy / tenantId
```

`synapse-data` 不依赖 `synapse-security`。如果当前用户来自 security，则由 security 负责把 `AuthenticatedUser` 适配为 `OperationContext`。

## 6. 快速使用

### 6.1 业务实体字段示例

业务系统实体只要拥有对应 setter，就可以被自动填充：

```java
public class SampleEntity {
    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer deleted;
    private Integer version;
    private String tenantId;
}
```

注意：实体类、表结构、Mapper 都属于业务系统，不属于 `synapse-data`。

### 6.2 在非 HTTP 场景中建立上下文

```java
try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
    sampleMapper.insert(entity);
}
```

如果没有 `OperationContext`，`createdBy` / `updatedBy` 不会被填充。

### 6.3 自定义审计信息来源

```java
@Bean
SynapseAuditorProvider synapseAuditorProvider() {
    return new SynapseAuditorProvider() {
        @Override
        public Optional<String> currentAuditor() {
            return Optional.of("system-job");
        }

        @Override
        public Optional<String> currentTenantId() {
            return Optional.empty();
        }
    };
}
```

## 7. 扩展方式

### 7.1 替换 MyBatis-Plus 插件链

```java
@Bean
MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
    interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
    return interceptor;
}
```

### 7.2 替换 ID 生成器

```java
@Bean
IdentifierGenerator identifierGenerator() {
    return new CustomIdentifierGenerator();
}
```

### 7.3 替换自动填充处理器

```java
@Bean
MetaObjectHandler metaObjectHandler() {
    return new CustomMetaObjectHandler();
}
```

### 7.4 替换 OperationContextProvider

```java
@Bean
OperationContextProvider operationContextProvider() {
    return new CustomOperationContextProvider();
}
```

## 8. 配置项

`synapse-data` 当前没有独立配置项。

行为主要通过 Bean 覆盖扩展：

- `MybatisPlusInterceptor`
- `Clock`
- `IdentifierGenerator`
- `OperationContextProvider`
- `SynapseAuditorProvider`
- `MetaObjectHandler`

## 9. 边界与注意事项

### 9.1 data 不依赖 security

不要在 data 模块中直接读取 `SecurityContext`。

正确链路是：

```text
security -> OperationContext -> data
```

而不是：

```text
data -> security
```

### 9.2 没有上下文时不默认使用 system

如果没有 OperationContext，当前默认不会填充 createdBy / updatedBy。

不应在 framework 中默认写死 `system`，否则异步、MQ、定时任务场景会失去追溯价值。

### 9.3 字段名是约定，不是强制基类

`synapse-data` 不要求业务实体继承某个 BaseEntity。

只要实体有对应 setter，MyBatis-Plus 自动填充就可以生效。

### 9.4 tenantId 只是承载位

`tenantId` 只是上下文和字段承载位，当前不实现：

- SQL 自动拼租户条件。

相关隔离规则应由业务系统或平台服务实现。

## 10. 常见问题

### Q1：为什么 data 不直接依赖 security 获取当前用户？

因为数据层不应该绑定认证实现。HTTP、MQ、Task、Async 都可能触发数据写入，统一通过 `OperationContext` 才能覆盖所有入口。

### Q2：为什么没有 BaseEntity？

BaseEntity 会强约束业务实体继承结构，不适合作为 framework 默认要求。当前采用字段名约定，更轻量。

### Q3：createdBy 没有自动填充怎么办？

检查是否已经建立 `OperationContext`，或者是否提供了 `SynapseAuditorProvider`。

### Q4：可以修改默认字段名吗？

当前未提供配置项。需要不同字段名时，可以替换 `MetaObjectHandler`。

### Q5：为什么分页插件 DbType 是 OTHER？

framework 不应该绑定具体数据库。业务系统可以覆盖 `MybatisPlusInterceptor` 并指定自己的数据库类型。
