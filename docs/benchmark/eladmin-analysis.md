# eladmin 对标分析

## 1. 项目定位

eladmin 是基于 Spring Boot 2.7.18、JPA、JWT、Spring Security、Redis、Vue 的前后端分离后台管理系统，采用分模块开发方式，权限控制采用 RBAC，支持数据字典、数据权限、一键生成前后端代码和动态路由。

来源：https://github.com/elunez/eladmin

## 2. 值得借鉴

### 2.1 后台模块边界

eladmin 的价值不在于持久化路线，而在于后台系统模块拆分：

- 系统管理
- 权限管理
- 数据字典
- 数据权限
- 代码生成
- 运维管理
- 日志管理
- 定时任务

Synapse 可以参考其模块边界，但不采用 JPA 路线。

### 2.2 数据权限

eladmin 明确支持数据权限管理。Synapse 应从 v0.1 开始预留数据权限抽象，而不是后期硬塞。

建议抽象：

```text
DataScope
├── ALL
├── SELF
├── DEPT
├── DEPT_AND_CHILDREN
├── CUSTOM_DEPT
└── CUSTOM_SQL_FRAGMENT 禁止在 v0.1 开放
```

### 2.3 动态路由

后台框架必须保证后端菜单和前端路由一致。Synapse 应设计统一的菜单模型：

```text
目录 Catalog
菜单 Menu
按钮 Button
外链 ExternalLink
内嵌 Iframe
API Permission
```

## 3. 不建议照搬

### 3.1 JPA 技术路线

用户当前主要技术路线是 MyBatis/MyBatis-Plus。Synapse 不采用 JPA 作为默认持久化层。

### 3.2 旧前端依赖

旧 Vue/Element UI 可参考交互，不作为技术基线。

### 3.3 动态查询注解不要过度魔法化

可以提供查询 DSL 和 QueryCriteria，但不要让注解变成黑盒。

## 4. 对 Synapse 的决策

采用：

- 系统管理边界
- 数据权限预留
- 动态路由
- 后台运维模块思路
- 代码生成器能力

拒绝：

- JPA 作为默认 ORM
- 旧前端技术栈
- 过度动态查询魔法

## 5. Codex 使用建议

涉及 eladmin 对标时，Codex 只能读取模块边界和功能清单，不能迁移 JPA 代码。
