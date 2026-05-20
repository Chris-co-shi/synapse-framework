# RuoYi-Vue 对标分析

## 1. 项目定位

RuoYi-Vue 是基于 Spring Boot + Vue 的前后端分离 Java 快速开发框架。

公开仓库说明中，它的关键能力包括：

- Vue + Element UI 前端
- Spring Boot + Spring Security + Redis + JWT 后端
- JWT 认证
- 多终端认证
- 动态权限菜单
- 代码生成器生成前后端代码

来源：https://github.com/yangzongzhuan/RuoYi-Vue

## 2. 值得借鉴

### 2.1 最小后台管理能力模型

RuoYi 最值得借鉴的是“后台管理系统最低配置”：

- 用户
- 角色
- 菜单
- 部门
- 岗位
- 字典
- 参数配置
- 通知公告
- 操作日志
- 登录日志
- 在线用户
- 定时任务
- 代码生成

这些模块应成为 Synapse v0.1 的系统管理模块基础。

### 2.2 动态菜单和权限模型

RuoYi 的菜单权限模型适合作为最小模型参考：

```text
用户 -> 角色 -> 菜单/按钮权限 -> 前端动态路由 + 后端权限校验
```

Synapse 应保留这个主链路，但不能直接照搬实现。

### 2.3 代码生成器

RuoYi 的代码生成器证明了后台框架必须具备“表结构 -> 前后端 CRUD”的能力。

Synapse Codegen 第一版应支持：

- Entity
- Mapper
- Repository Adapter
- Repository Port
- Application Service
- Controller
- DTO / VO / Command
- Vue 页面
- TypeScript API
- Flyway SQL migration

## 3. 不建议照搬

### 3.1 包结构不适合作为长期框架边界

RuoYi 更偏快速开发模板，分层边界不够适合作为长期框架。Synapse 应采用：

```text
interfaces -> application -> domain -> infrastructure
```

### 3.2 旧前端技术栈

RuoYi-Vue 的 Vue + Element UI 对现代新项目不够理想。Synapse 应采用：

- Vue 3
- TypeScript
- Vite
- Pinia
- Element Plus 或 Naive UI

### 3.3 业务代码和框架代码容易混在一起

Synapse 必须把框架基础能力与业务模块分离：

```text
framework modules != business modules
```

## 4. 对 Synapse 的决策

采用：

- 系统管理模块清单
- RBAC 主链路
- 动态菜单思想
- 代码生成器思路

拒绝：

- 老包结构
- 直接复制工具类
- 直接复用前端结构
- Controller 直连持久化的开发习惯

## 5. Codex 使用建议

让 Codex 分析 RuoYi 时，只允许输出：

- 模块清单
- 表关系
- 接口清单
- 权限链路
- 代码生成输入输出

禁止复制源码。
