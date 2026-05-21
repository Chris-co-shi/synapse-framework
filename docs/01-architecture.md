# Synapse Framework 架构设计

## 1. 总体架构

```text
┌──────────────────────────────────────────────┐
│                Admin UI / Client             │
│        Vue3 / TS / Vite / Element Plus        │
└──────────────────────┬───────────────────────┘
                       │ HTTP / JSON
┌──────────────────────▼───────────────────────┐
│             interfaces / controller           │
│      REST API / Request Validation / VO       │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│               application layer               │
│     UseCase / Command / Query / Transaction   │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│                 domain layer                  │
│ Domain Model / Domain Service / Repository Port│
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│             infrastructure layer              │
│ MyBatis-Plus / Redis / MQ / File / External   │
└──────────────────────────────────────────────┘
```

## 2. 模块架构

```text
synapse-framework
├── synapse-bom
├── synapse-common
├── synapse-web
├── synapse-data
├── synapse-cache
├── synapse-security
├── synapse-audit
├── synapse-tenant
├── synapse-codegen
├── synapse-admin-api
└── synapse-admin-ui
```

## 3. 模块职责

### 3.1 synapse-bom

统一依赖版本管理。

职责：

- Spring Boot 版本
- MyBatis-Plus 版本
- Spring Security 版本
- Jackson/MapStruct/Lombok/Knife4j 等版本
- 测试依赖版本

禁止：

- 放业务代码
- 放配置类

### 3.2 synapse-common

通用基础能力。

职责：

- 错误码基类
- 业务异常
- 通用枚举
- ID 生成接口
- 时间工具
- 脱敏工具
- 通用常量

禁止：

- 依赖 Spring Web
- 依赖 MyBatis-Plus
- 放业务模块常量

### 3.3 synapse-web

Web 层通用能力。

职责：

- 统一响应结构
- 统一异常处理
- 参数校验错误处理
- 分页请求/响应
- 请求上下文
- Trace ID
- WebMvc 配置

### 3.4 synapse-data

数据访问基础能力。

职责：

- BaseEntity
- BaseLogEntity
- MyBatis-Plus 完整能力配置
- dynamic-datasource 配置级多数据源切换
- 数据库方言适配层
- 分页插件
- 乐观锁插件
- 逻辑删除配置
- 租户插件预留
- 自动填充字段

### 3.5 synapse-cache

缓存与并发控制基础能力。

职责：

- Redis 缓存配置
- Redis Lua 脚本执行封装
- 可重入分布式锁
- 滑动窗口限流
- 缓存 key 命名规范

### 3.6 synapse-security

认证与授权基础能力。

职责：

- OAuth2 Authorization Server
- OAuth2 Resource Server
- JWT + JWK
- PasswordEncoder
- LoginUser
- SecurityContext
- 权限注解
- Token 黑名单
- 接口权限校验

### 3.7 synapse-audit

审计日志。

职责：

- 操作日志注解
- 登录日志
- 安全事件日志
- 审计事件模型
- 审计持久化接口

### 3.8 synapse-tenant

租户上下文与租户隔离预留。

职责：

- TenantContext
- TenantAware
- TenantIgnore
- tenant_id 字段规则
- MyBatis-Plus TenantLineHandler 适配预留

### 3.9 synapse-codegen

代码生成器。

职责：

- 读取表结构或元数据
- 生成后端标准分层代码
- 生成前端页面和 API
- 生成 migration
- 生成测试骨架

### 3.10 synapse-admin-api

后台管理 API。

职责：

- 用户管理
- 角色管理
- 菜单管理
- 权限管理
- 字典管理
- 参数配置
- 操作日志
- 登录日志

### 3.11 synapse-admin-ui

后台管理前端。

职责：

- 登录页
- 布局
- 动态路由
- 菜单渲染
- 用户管理页面
- 角色管理页面
- 菜单管理页面
- 字典管理页面

## 4. 依赖方向

允许：

```text
admin-api -> security/web/data/common
security -> web/common/cache
web -> common
 data -> common
cache -> common
 audit -> common/web/data/security
 codegen -> common
```

禁止：

```text
common -> web
common -> data
common -> security
common -> admin-api
domain -> infrastructure
controller -> mapper
frontend -> database
```

## 5. 请求链路

```text
HTTP Request
  -> Filter / Security
  -> Controller
  -> Request DTO Validation
  -> Application Service
  -> Domain Model / Domain Service
  -> Repository Port
  -> Repository Adapter
  -> Mapper / Entity
  -> Database
```

## 6. 响应链路

```text
Database
  -> Entity
  -> Repository Adapter
  -> Domain Model
  -> Application Result
  -> VO
  -> ApiResponse
  -> HTTP Response
```

## 7. 横切能力

横切能力通过 starter/configuration/annotation 实现：

- 认证
- 授权
- 审计日志
- Trace ID
- 异常处理
- 参数校验
- Redis 缓存
- Redis Lua 分布式锁
- Redis Lua 限流
- 动态数据源
- 数据权限
- 多租户
- 幂等
- 限流

## 8. 演进路线

```text
v0.1 通用技术基座 + Starter + 示例应用
v0.2 IAM/Auth/RBAC 验证模块 + 数据权限 + 租户预留落地
v0.3 代码生成器增强 + 文件/消息/通知
v0.4 微服务版本 + Gateway + Nacos + OpenFeign
v0.5 工作流适配 + 企业业务模块模板
```
