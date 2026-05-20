# Synapse Framework 实施路线图

## 阶段 0：对标分析

状态：本文档包已完成初版。

交付物：

- benchmark 总览
- RuoYi-Vue 分析
- eladmin 分析
- ruoyi-vue-pro 分析
- JeecgBoot 分析
- Spring Boot Admin 分析
- SpringBlade 分析
- 设计决策记录

## 阶段 1：框架规则

状态：本文档包已完成初版。

交付物：

- 项目定位
- 总体架构
- 模块边界
- 包结构规则
- 数据库规则
- API 规则
- 安全规则
- 测试规则
- AI 协作规则
- AGENTS.md
- Skills 初版

## 阶段 2：工程骨架

目标：搭建 Maven 多模块工程。

模块：

```text
synapse-bom
synapse-common
synapse-web
synapse-data
synapse-security
synapse-audit
synapse-admin-api
```

完成标准：

- `mvn test` 通过
- 统一响应可用
- 统一异常可用
- Flyway 可用
- MyBatis-Plus 基础配置可用

## 阶段 3：Auth 最小闭环

目标：完成登录、刷新、登出、me。

接口：

```text
POST /api/admin/auth/login-options
POST /api/admin/auth/login
POST /api/admin/auth/refresh
POST /api/admin/auth/logout
GET  /api/admin/auth/me
```

完成标准：

- access token 可用
- refresh token rotation 可用
- logout 校验 token 归属
- 登录失败锁定可用
- 登录日志可用
- 测试覆盖主要异常场景

## 阶段 4：RBAC 系统管理

目标：完成用户、角色、菜单、权限。

模块：

- user
- role
- menu
- permission

完成标准：

- 用户 CRUD
- 角色 CRUD
- 菜单 CRUD
- 用户分配角色
- 角色分配菜单
- 登录后加载动态菜单
- 接口权限校验

## 阶段 5：基础系统模块

目标：完成后台管理常见基础模块。

模块：

- dept
- post
- dict
- config
- operation-log
- login-log

完成标准：

- 数据字典可用
- 参数配置可用
- 操作日志可查询
- 登录日志可查询

## 阶段 6：代码生成器 v0.1

目标：通过表结构/元数据生成标准 CRUD。

输出：

- Entity
- Mapper
- Repository Port
- Repository Adapter
- Application Service
- Controller
- DTO / VO / Command
- Vue 页面
- TS API
- Test skeleton
- Flyway migration skeleton

完成标准：

- 生成代码能编译
- 生成代码符合包结构规则
- 生成代码不破坏分层
- 生成代码有基础测试

## 阶段 7：框架验证业务模块

目标：用一个小型业务模块验证框架通用性。

建议模块：

- 资产管理 asset
- 工单管理 work-order
- 客户管理 customer

完成标准：

- 代码生成器生成基础代码
- 人工补业务规则
- 权限/审计/字典/分页均可复用

## 阶段 8：生产化增强

目标：补齐工程质量。

内容：

- CI/CD
- Dockerfile
- GitHub Actions
- Helm Chart
- Actuator
- Spring Boot Admin 集成预留
- Testcontainers
- 性能基准
- 安全扫描
