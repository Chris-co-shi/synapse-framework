# synapse-data Skill

## 1. 模块职责

`synapse-data` 提供 Synapse Framework 的数据访问基础能力。

职责：

- BaseEntity
- BaseLogEntity
- MyBatis-Plus 配置
- 分页插件
- 乐观锁插件
- 逻辑删除配置
- 自动填充字段
- dynamic-datasource 配置级多数据源切换
- 数据库方言适配层
- Flyway migration 规则
- PostgreSQL Flyway 支持必须包含 `flyway-database-postgresql`，否则 Docker 可用时 PostgreSQL migration 测试会失败。
- Repository Adapter 标准实现模式
- Wrapper 安全约束
- Data 层测试规范

不负责：

- 业务领域规则。
- Controller。
- 具体业务 Service。
- 数据库集群高可用、读写分离、故障转移。
- 运行时动态增删数据源。
- 分布式事务。

## 2. 执行前必须读取

涉及 Data / MyBatis-Plus / Entity / Mapper / Repository / Migration 的任务必须读取：

```text
AGENTS.md
docs/01-architecture.md
docs/02-module-boundary.md
docs/04-database-rules.md
docs/07-test-rules.md
docs/08-ai-development-rules.md
docs/10-technical-foundation-baseline.md
skills/synapse-data/SKILL.md
当前 task 文件
```

禁止只读 task 就直接写代码。

## 3. ID 强制规则

所有业务主键 ID 统一：

```text
Java 类型：String
数据库类型：varchar(19)
```

禁止：

```java
private Long id;
Optional<User> findById(Long id);
void deleteById(Long id);
```

必须：

```java
private String id;
Optional<User> findById(String id);
void deleteById(String id);
```

禁止：

```sql
id bigint primary key
```

必须：

```sql
id varchar(19) primary key
```

说明：

- 禁止把业务主键设计成 Java `Long`。
- 禁止把业务主键设计成数据库长整型。
- 如果代码中出现长整型，只能是统计数量、排序值、版本外的普通数值字段，不能作为业务主键 ID。

## 4. Entity 强制规则

Entity 只允许放在：

```text
infrastructure.persistence.entity
```

业务持久化 Entity 推荐使用：

```java
@Getter
@Setter
```

例外：

- `synapse-data` 模块内的 `BaseEntity`、`BaseLogEntity` 等框架基础实体必须优先保证 `mvn clean test` 下稳定编译。
- 如果当前 JDK/Maven/Lombok 注解处理链不稳定，基础实体允许显式 getter/setter/构造器，避免基础模块构建依赖注解生成。

禁止：

- 在普通业务 Entity 中无理由手写大量 getter/setter。
- 暴露到 Controller。
- 作为接口入参。
- 作为接口响应。
- 放业务行为。
- 作为跨模块领域对象。
- 让 Domain Model 依赖 MyBatis-Plus。
- Controller 直接返回 Entity。

Entity 示例：

```java
@Getter
@Setter
@TableName("iam_user")
public class UserEntity extends BaseEntity {

    @TableField("username")
    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("status")
    private Integer status;
}
```

## 5. DTO / Request / Response / Command / Result 规则

默认推荐使用：

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
```

示例：

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String username;

    private Integer status;
}
```

禁止无理由手写 getter/setter；如果存在编译链、序列化兼容或框架基础类稳定性原因，必须在任务说明或 Skill 中说明。

## 6. BaseEntity 规则

普通业务表使用 BaseEntity：

```java
@Getter
@Setter
public abstract class BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
```

字段要求：

| 字段 | Java 类型 | DB 类型 |
|---|---|---|
| id | String | varchar(19) |
| tenantId | String | varchar(19) |
| createdAt | Instant | timestamp |
| createdBy | String | varchar(19) |
| updatedAt | Instant | timestamp |
| updatedBy | String | varchar(19) |
| deleted | Integer | smallint |
| version | Integer | integer |

当前 Foundation 第一层要求：

- `BaseEntity` 作为框架基础类必须在 clean build 下不依赖未稳定注解处理。
- 修改 `BaseEntity` 字段类型、构造器或访问器后，必须运行根目录 `mvn clean test`。
- 如果后续重新启用 Lombok，必须先补齐 Lombok 编译配置和 clean build 验证，再更新本 Skill。

## 7. BaseLogEntity 规则

日志表使用 BaseLogEntity：

```java
@Getter
@Setter
public abstract class BaseLogEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;
}
```

日志表通常：

- 不做频繁更新。
- 不需要乐观锁。
- 是否逻辑删除视查询需求决定。

## 8. MyBatis-Plus 基座规则

`synapse-data` 已固定使用：

```text
mybatis-plus-spring-boot3-starter
mybatis-plus-jsqlparser
```

原因：

- MP 3.5.9 的分页插件依赖官方 `mybatis-plus-jsqlparser`。
- 需要把分页插件、乐观锁插件、基础实体注解和自动填充纳入 Data Foundation。

默认自动配置：

- `SynapseDataAutoConfiguration`
- `MybatisPlusInterceptor`
- `PaginationInnerInterceptor`
- `OptimisticLockerInnerInterceptor`
- `SynapseMetaObjectHandler`
- `SynapseAuditorProvider`

自动填充规则：

- insert 填充 `createdAt`、`updatedAt`、`deleted`、`version`。
- 当前审计人存在时填充 `createdBy`、`updatedBy`。
- update 覆盖 `updatedAt`，当前审计人存在时覆盖 `updatedBy`。
- 填充处理器必须允许纯单元测试，不依赖 MP 运行期 `TableInfo` 缓存才能执行。

禁止：

- 业务模块重复声明分页插件或乐观锁插件。
- 在业务 Mapper 中绕过基座自动填充规则。
- 让 Controller、DTO、Domain Model 直接依赖 MP Entity。
- 使用 `IService` / `ServiceImpl` 暴露到 application 或 web 层。

## 9. dynamic-datasource 使用边界

`synapse-data` 只提供配置级多数据源切换基座，不实现数据库集群高可用、读写分离策略和运行时数据源增删。

允许：

- 使用 `@SynapseDataSource("name")` 标记 Repository Adapter 或 Adapter 方法。
- 使用 `DataSourceContext.scope("name")` 在框架内部做小范围数据源上下文切换。
- 使用 try-with-resources 保证上下文恢复。

示例：

```java
try (DataSourceScope ignored = DataSourceContext.scope("reporting")) {
    // 调用 reporting 数据源下的持久化逻辑
}
```

禁止：

- 在 Controller 层切换数据源。
- 从前端请求参数直接决定数据源名称。
- 在没有 finally/try-with-resources 的情况下手动 `use()` 后忘记 `clear()`。
- 把动态数据源当成完整高可用方案；高可用由后续 Data 增强任务单独设计。

## 10. 数据库方言规则

通过 `DatabaseDialectResolver` 从 JDBC URL 或显式 `DatabaseType` 获取方言能力。

当前能力：

- PostgreSQL：支持 partial index，支持 JSON column。
- MySQL：不声明 partial index，支持 JSON column。
- H2：测试方言，能力保守。
- UNKNOWN：默认保守。

禁止：

- 在业务代码中直接按数据库产品名散落 `if/else`。
- 在 migration 中写无法被目标数据库支持的 SQL 而没有方言说明。

## 11. MyBatis-Plus 使用边界

允许：

- BaseMapper
- ActiveRecord `Model<T>`
- LambdaQueryWrapper
- LambdaUpdateWrapper
- 分页插件
- 乐观锁插件
- 逻辑删除
- 自动填充
- 租户插件预留

禁止：

- Controller 直接调用 Mapper。
- Controller 直接调用 IService / ServiceImpl / ActiveRecord 模型。
- Wrapper 条件来自未校验前端字段。
- 拼接 SQL 片段。
- ActiveRecord 穿透到 Web/API 层。
- Domain Model 继承 MyBatis-Plus Model。

建议：

- 基础 CRUD 可以在 infrastructure 内部使用 MyBatis-Plus 提效。
- 跨聚合编排、事务协调、权限审计等仍放在 application service。
- Repository Port 对外暴露领域语义。
- Repository Adapter 内部使用 MyBatis-Plus。

## 12. Mapper 规则

Mapper 位于：

```text
infrastructure.persistence.mapper
```

示例：

```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
```

禁止：

- Controller 注入 Mapper。
- Application Service 直接注入 Mapper。
- Domain Service 直接注入 Mapper。
- Mapper 返回接口响应对象。

## 13. Repository Port / Adapter 规则

Repository Port 位于：

```text
domain.repository
```

Repository Adapter 位于：

```text
infrastructure.persistence.repository
```

Repository Port 示例：

```java
public interface UserRepository {

    Optional<User> findById(String id);

    Optional<User> findByUsername(String username);

    User save(User user);

    void deleteById(String id);
}
```

Repository Adapter 示例：

```java
@Repository
@RequiredArgsConstructor
class MybatisPlusUserRepository implements UserRepository {

    private final UserMapper userMapper;
    private final UserPersistenceConverter converter;

    @Override
    public Optional<User> findById(String id) {
        UserEntity entity = userMapper.selectById(id);
        return Optional.ofNullable(entity).map(converter::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = converter.toEntity(user);
        userMapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public void deleteById(String id) {
        userMapper.deleteById(id);
    }
}
```

## 14. Converter 规则

Converter 位于：

```text
infrastructure.persistence.converter
```

负责：

```text
Entity -> Domain
Domain -> Entity
```

禁止：

- Controller 手写大量字段转换。
- Entity 直接返回前端。
- Domain 直接 JSON 序列化给前端。
- Converter 写业务规则。

可以使用 MapStruct，也可以在早期手写静态转换，但要保持集中管理。

## 15. Flyway migration 规则

命名：

```text
V202605200001__create_iam_user.sql
V202605200002__create_iam_role.sql
```

规则：

- migration 一旦合入主分支不得修改。
- 修复用新 migration。
- seed 数据单独 migration。
- 生产环境禁止 clean。
- 业务主键统一使用 `varchar(19)`。
- 禁止业务主键使用数据库长整型。
- Data Foundation 的 migration 验证必须至少覆盖 H2 可执行。
- 涉及数据库兼容能力时，再用 Testcontainers 增加 PostgreSQL/MySQL 验证。
- Testcontainers 测试必须在 Docker 可用时真实执行；本地 Docker 不可用时允许跳过，但最终 CI 环境应提供 Docker。

普通业务表示例：

```sql
create table iam_user (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    username varchar(64) not null,
    password_hash varchar(255) not null,
    status smallint not null default 1,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);
```

日志表示例：

```sql
create table sys_login_log (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    user_id varchar(19) null,
    username varchar(64) null,
    login_time timestamp not null,
    login_status smallint not null,
    failure_reason varchar(255) null,
    created_at timestamp not null,
    created_by varchar(19) null
);
```

## 16. Wrapper 安全规则

允许：

```java
Wrappers.<UserEntity>lambdaQuery()
    .eq(UserEntity::getUsername, username)
    .eq(UserEntity::getDeleted, 0);
```

禁止：

```java
queryWrapper.apply(request.getCondition());
queryWrapper.last(request.getSqlTail());
queryWrapper.orderBy(true, true, request.getSortField());
```

排序字段必须走白名单映射：

```java
private static final Map<String, SFunction<UserEntity, ?>> SORT_FIELD_MAPPING = Map.of(
    "createdAt", UserEntity::getCreatedAt,
    "username", UserEntity::getUsername
);
```

## 17. 乐观锁规则

涉及并发更新的表必须有 `version`。

场景：

- 用户状态变更
- 角色权限变更
- 菜单排序变更
- 库存类业务表
- 配置修改

更新失败必须转成业务异常：

```text
DATA_CONFLICT
```

## 18. 逻辑删除规则

默认：

```text
deleted = 0 未删除
deleted = 1 已删除
```

唯一索引注意：

```sql
unique(tenant_id, username, deleted)
```

或采用 partial unique index，具体取决于数据库。

## 19. 多租户规则

v0.1 预留 `tenant_id`。

必须明确：

- 哪些表需要 tenant_id。
- 哪些表是全局表。
- 哪些查询必须带 tenant_id。
- 哪些系统管理操作可以跨租户。

禁止：

- 从前端请求体直接信任 tenant_id。
- 手写 SQL 忘记 tenant_id。
- 允许普通用户传 tenant_id 越权查询。

## 20. 时间字段规则

系统审计时间统一用 `Instant`：

```java
private Instant createdAt;
private Instant updatedAt;
```

数据库按 UTC 语义保存。

业务本地时间使用：

```java
private LocalDateTime localDateTime;
private String zoneId;
```

涉及未来排程、跨国家、夏令时的业务，禁止只保存 `LocalDateTime` 或只保存 `ZoneOffset`。

## 21. 测试要求

Data 相关任务至少覆盖：

- Entity 映射能编译。
- BaseEntity / BaseLogEntity MP 注解。
- MyBatis-Plus 自动配置。
- 自动填充处理器。
- dynamic-datasource 注解封装。
- DataSourceContext scope 恢复。
- 数据库方言解析。
- Flyway migration 可执行。
- PostgreSQL Testcontainers migration 验证；本地 Docker 不可用时允许跳过，CI 应执行。
- Mapper 基础 CRUD。
- Repository Adapter 正常转换。
- 逻辑删除。
- 乐观锁冲突。
- 租户字段。
- 唯一约束冲突。
- 分页查询。
- 排序白名单。
- Flyway migration 可执行。

命令：

```bash
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-data -am test
```

关键改动：

```bash
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test
```

模块完成后必须满足：

- 模块级测试通过。
- 根目录 `mvn clean test` 通过。
- `skills/synapse-data/SKILL.md` 已根据稳定实现更新。
- `SKILL.md` 只记录职责、边界、模式、测试要求和常见错误，不写过程日志。

## 22. 常见错误

禁止出现：

```java
private Long id;
```

禁止出现：

```sql
id bigint primary key
```

禁止出现：

```java
public String getUsername() {
    return username;
}
```

例外：`BaseEntity`、`BaseLogEntity` 等框架基础类为了 clean build 稳定性允许显式访问器。

禁止：

- Entity 返回给 Controller。
- Controller 直接调用 Mapper。
- Domain Model 依赖 MyBatis-Plus。
- Repository Port 使用 MyBatis-Plus Entity。
- Wrapper 拼接未校验 SQL。
- 排序字段直接来自前端。
- 修改已合入主分支的 migration 历史文件。
- 只引入 MP starter 却忘记 `mybatis-plus-jsqlparser`。
- 在纯单元测试无法执行的自动填充逻辑中强依赖 MP `TableInfo` 缓存。
- 数据源上下文切换后没有恢复。

## 23. 示例任务拆分

推荐：

```text
TASK-003-01-synapse-data-base-entity
TASK-003-02-synapse-data-mybatis-plus-config
TASK-003-03-synapse-data-dynamic-datasource
TASK-003-04-synapse-data-flyway-baseline
TASK-003-05-synapse-data-repository-template
```

不推荐：

```text
一次性实现完整 Data 模块
一次性实现完整 IAM 用户角色权限
一次性生成所有后台 CRUD
```

## 24. 执行前自查

Data 任务开始前必须回答：

```text
1. 本次涉及哪些表？
2. 每张表对应哪个 Entity？
3. Entity 放在哪个包？
4. Mapper 放在哪个包？
5. Repository Port 放在哪个包？
6. Repository Adapter 放在哪个包？
7. 是否会使用 String id？
8. 是否会使用 varchar(19) 主键？
9. 是否会避免 Long id？
10. 是否会避免数据库长整型业务主键？
11. 是否会使用 Lombok？clean build 下是否稳定？
12. 是否会手写 getter/setter？如果会，是否属于基础类稳定性例外？
13. 是否会让 Domain Model 依赖 MyBatis-Plus？
14. 是否会让 Controller 直接依赖 Mapper？
15. 是否需要新增 migration？
16. 是否需要新增测试？
```
