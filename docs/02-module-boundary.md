# 模块边界规则

## 1. 总原则

模块边界是 Synapse Framework 的核心资产。任何功能实现前必须明确：

1. 属于框架基础能力？
2. 属于后台系统管理能力？
3. 属于业务扩展能力？
4. 属于外部适配能力？

## 2. 模块分类

### 2.1 Framework Core

框架核心模块：

- synapse-common
- synapse-web
- synapse-data
- synapse-security
- synapse-audit
- synapse-cache
- synapse-tenant

原则：

- 只放通用能力。
- 不放具体业务。
- 不依赖 admin-api。

### 2.2 Admin System

后台系统管理模块：

- 用户
- 角色
- 菜单
- 权限
- 部门
- 岗位
- 字典
- 参数
- 通知
- 日志
- 文件
- 定时任务

原则：

- 可以依赖框架核心。
- 不反向污染框架核心。

### 2.3 Extension Modules

扩展模块：

- workflow-adapter
- message
- file-storage
- monitor
- report
- plugin
- codegen

原则：

- 通过 SPI/Adapter 接入。
- 不强制所有项目启用。

### 2.4 Business Modules

业务模块：

- MES
- WMS
- QMS
- CRM
- ERP
- OA

原则：

- 永远不放进 framework core。
- 必须依赖框架，而不是被框架依赖。

## 3. v0.1 系统管理模块清单

```text
system-user
system-role
system-menu
system-permission
system-dept
system-post
system-dict
system-config
system-notice
system-operation-log
system-login-log
```

## 4. 推荐表清单

```text
iam_user
iam_role
iam_menu
iam_permission
iam_user_role
iam_role_menu
iam_role_permission
iam_dept
iam_post
iam_user_post
sys_dict_type
sys_dict_item
sys_config
sys_notice
sys_operation_log
sys_login_log
```

## 5. 模块之间禁止事项

- system-user 不直接操作 system-role 的 Mapper。
- system-role 不直接操作 system-menu 的 Mapper。
- 权限分配通过 application service 协调。
- 跨模块读取必须通过 port/service 接口。
- 禁止循环依赖。
- 禁止把多个模块混进一个巨大 service。

## 6. 模块交互示例

用户分配角色：

```text
UserRoleApplicationService
  -> UserRepository
  -> RoleRepository
  -> UserRoleRepository
  -> AuditLogPort
```

角色分配菜单：

```text
RoleMenuApplicationService
  -> RoleRepository
  -> MenuRepository
  -> RoleMenuRepository
  -> PermissionCacheEvictPort
```

登录加载权限：

```text
AuthApplicationService
  -> UserRepository
  -> RoleRepository
  -> MenuRepository
  -> PermissionRepository
  -> TokenService
  -> LoginLogPort
```

## 7. 判断一个能力放在哪

| 能力 | 放置位置 |
|---|---|
| 统一异常 | synapse-web |
| 错误码基类 | synapse-common |
| JWT 解析 | synapse-security |
| 操作日志注解 | synapse-audit |
| BaseEntity | synapse-data |
| 用户管理 | synapse-admin-api |
| 字典管理 | synapse-admin-api |
| 代码生成器 | synapse-codegen |
| 工作流 | synapse-workflow-adapter，v0.1 不做 |
| MES 工单 | business module，不进 framework |
