# synapse-mybatis-plus 使用手册

## 1. 模块定位

`synapse-mybatis-plus` 是 MyBatis-Plus 工程增强模块。

它承接 MyBatis-Plus 专属能力；`synapse-data` 继续保持 ORM 无关。

## 2. 当前能力

当前模块提供：

- MyBatis-Plus 自动配置；
- 默认 `MybatisPlusInterceptor` 插件链；
- 分页、乐观锁、防全表 update/delete 和非法 SQL 插件开关；
- `OperationContext` 到 `DataAuditorProvider` 的审计适配；
- 创建和修改审计字段自动填充；
- 安全排序字段白名单；
- ID 生成器适配；
- `PageQuery` / `PageResult` 与 MyBatis-Plus `Page` 转换；
- 可选择继承的持久化实体基类。

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

## 5. 实体基类

模块提供以下继承链：

```text
IdEntity
  └── CreatedEntity
       └── MutableEntity
            └── VersionedEntity
                 └── ManagedEntity
```

| 类型 | 字段 | 能力 |
| --- | --- | --- |
| `IdEntity` | `String id` | `ASSIGN_ID` 字符串主键 |
| `CreatedEntity` | `createdAt`, `createdBy` | 创建审计 |
| `MutableEntity` | `updatedAt`, `updatedBy` | 修改审计 |
| `VersionedEntity` | `revision` | MyBatis-Plus 乐观锁 |
| `ManagedEntity` | `deleted` | 乐观锁和逻辑删除 |

消费方应根据实体真实生命周期选择满足需求的最浅基类：

```java
@TableName("user_account")
public class UserEntity extends VersionedEntity {
    private String username;
    private String status;
}
```

约束：

- 这些类型只用于 MyBatis-Plus 持久化 Entity；
- Domain Model、DTO、Command、Query 和 Event 不得继承；
- 不要求所有 Entity 继承 `ManagedEntity`；
- 关系表、日志、Outbox、会话和执行记录可以不继承基类；
- `revision` 只表示技术乐观锁版本，不表示业务修订版；
- 基类不统一实现 `equals/hashCode`，具体实体按业务身份语义自行决定。

## 6. 字段规则

### 6.1 ID

`IdEntity` 使用：

```java
@TableId(type = IdType.ASSIGN_ID)
private String id;
```

数据库字段长度由消费方数据库规范决定。Platform 当前推荐 PostgreSQL `varchar(19)`。

### 6.2 审计字段

- `createdAt`、`createdBy`：`FieldFill.INSERT`；
- `updatedAt`、`updatedBy`：`FieldFill.INSERT_UPDATE`；
- 时间字段类型为 `Instant`；
- 插入时只填充空字段；
- 更新时刷新 `updatedAt` 和可用的 `updatedBy`。

### 6.3 乐观锁

`VersionedEntity` 使用：

```java
@Version
private Integer revision;
```

模块不主动初始化 `revision`，数据库应提供明确默认值，消费方应测试并发更新冲突。

### 6.4 逻辑删除

`ManagedEntity` 使用：

```java
@TableLogic(value = "0", delval = "1")
private Integer deleted;
```

模块不主动初始化 `deleted`，数据库应提供明确默认值。逻辑删除是显式选择的实体能力，不是全局业务规则。

## 7. OperationContext 审计适配

默认自动配置注册 `OperationContextDataAuditorProvider`，从当前 `OperationContext` 读取 actor id，用于填充 `createdBy` 和 `updatedBy`。

- 没有上下文时不填充审计人；
- `UNKNOWN` actor 不写入；
- `SYSTEM` 只有入口方显式创建上下文时才写入；
- 不填充 `tenantId`；
- 消费方可以覆盖 `DataAuditorProvider`。

## 8. 安全排序

默认分页转换不接受任意外部排序字段。消费方必须配置白名单：

```java
Page<UserEntity> page = MybatisPlusPageConverter.toPage(
        query,
        SortFieldMapping.of(Map.of("createdAt", "created_at"))
);
```

未进入白名单的字段会被忽略。

## 9. 插件扩展

消费方可以提供 `SynapseInnerInterceptorContributor` Bean，在默认插件之后追加自定义 `InnerInterceptor`。

## 10. 边界

本模块不提供：

- 具体业务 Entity、Mapper、Repository 或 Service；
- 业务表和 migration；
- DataSource 治理；
- SQL 自动读写路由；
- 租户 SQL 和数据权限；
- 业务领域的删除、版本与相等性规则。
