# 数据库与持久化规则

## 1. 数据库治理原则

- 所有 DDL 必须走 Flyway migration。
- 禁止应用启动时自动改表。
- 表结构变更必须可追溯。
- 字段语义必须明确。
- 删除默认逻辑删除。
- 并发更新默认考虑乐观锁。
- 多租户预留必须从第一版开始设计。

## 2. 命名规范

### 2.1 表名

- 使用小写下划线。
- IAM 相关表使用 `iam_` 前缀。
- 系统通用表使用 `sys_` 前缀。
- 审计日志可使用 `audit_` 前缀。

示例：

```text
iam_user
iam_role
iam_menu
sys_dict_type
sys_dict_item
sys_config
audit_operation_log
```

### 2.2 字段名

使用小写下划线：

```text
id
tenant_id
created_at
created_by
updated_at
updated_by
deleted
version
```

## 3. 基础字段

普通业务表默认包含：

```sql
id BIGINT PRIMARY KEY,
tenant_id BIGINT NULL,
created_at TIMESTAMP NOT NULL,
created_by BIGINT NULL,
updated_at TIMESTAMP NOT NULL,
updated_by BIGINT NULL,
deleted SMALLINT NOT NULL DEFAULT 0,
version INTEGER NOT NULL DEFAULT 0
```

日志表可以不需要 `version`，是否需要 `deleted` 视查询需求决定。

## 4. Entity 规则

Entity 只存在于：

```text
infrastructure.persistence.entity
```

Entity 可以使用：

- `@TableName`
- `@TableId`
- `@TableField`
- `@TableLogic`
- `@Version`
- Lombok getter/setter

Entity 禁止：

- 暴露到 Controller
- 作为接口返回对象
- 放业务行为
- 继承 MyBatis-Plus `Model<T>`

## 5. BaseEntity 判断规则

### 5.1 BaseEntity

适合普通业务表：

- 有租户
- 有创建/更新时间
- 有逻辑删除
- 有乐观锁

### 5.2 BaseLogEntity

适合日志表：

- 操作日志
- 登录日志
- 审计事件
- 安全事件

日志表通常：

- 不做逻辑删除或谨慎做。
- 不做频繁更新。
- 不需要乐观锁。

## 6. MyBatis-Plus 使用边界

允许：

- BaseMapper
- LambdaQueryWrapper
- LambdaUpdateWrapper
- 分页插件
- 乐观锁插件
- 逻辑删除
- 自动填充
- 租户插件预留

默认禁止：

- IService / ServiceImpl 作为业务 Service 基类
- ActiveRecord `Model<T>`
- Controller 直接调用 Mapper
- Wrapper 条件来自未校验前端字段
- 拼接 SQL 片段

## 7. Wrapper 安全规则

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

排序字段必须走白名单映射。

## 8. 乐观锁规则

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

## 9. 逻辑删除规则

默认：

```text
deleted = 0 未删除
deleted = 1 已删除
```

唯一索引注意：

```sql
UNIQUE(tenant_id, username, deleted)
```

或采用 partial unique index，具体取决于数据库。

## 10. 多租户规则

v0.1 预留 `tenant_id`。

必须明确：

- 哪些表需要 tenant_id
- 哪些表是全局表
- 哪些查询必须带 tenant_id
- 哪些系统管理操作可以跨租户

禁止：

- 从前端请求体直接信任 tenant_id。
- 手写 SQL 忘记 tenant_id。
- 允许普通用户传 tenant_id 越权查询。

## 11. Flyway 规则

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

## 12. 索引规则

索引必须服务查询场景。

常见索引：

```sql
CREATE INDEX idx_iam_user_tenant_username ON iam_user(tenant_id, username);
CREATE INDEX idx_iam_user_tenant_status ON iam_user(tenant_id, status);
CREATE INDEX idx_sys_operation_log_created_at ON sys_operation_log(created_at);
```

禁止：

- 每个字段都建索引。
- 建了索引但没有查询场景说明。

## 13. 数据库兼容策略

v0.1 推荐 PostgreSQL 优先，MySQL 兼容预留。

注意：

- JSON 字段慎用。
- 时间字段统一 UTC。
- 不依赖某个数据库独有特性作为核心链路。
- 需要数据库差异时放 adapter。
