# ruoyi-vue-pro 对标分析

## 1. 项目定位

ruoyi-vue-pro 是基于 Spring Boot + MyBatis Plus + Vue & Element 的后台管理系统，公开说明中包含 RBAC 动态权限、数据权限、SaaS 多租户、Flowable 工作流、三方登录、支付、短信、商城、CRM、ERP、MES、IM、AI 大模型、IoT 等能力。

来源：https://github.com/YunaiV/ruoyi-vue-pro

## 2. 值得借鉴

### 2.1 企业能力扩展模型

ruoyi-vue-pro 的价值是证明后台框架最终会演进到“基础框架 + 大量业务扩展模块”。

Synapse 应采用类似分层：

```text
基础层：common/web/data/security/cache/audit
系统层：admin/iam/system
扩展层：tenant/message/file/workflow/codegen
业务层：MES/WMS/QMS/CRM/ERP/OA
```

### 2.2 多租户

Synapse v0.1 不做完整 SaaS，但必须保留字段和上下文：

- `tenant_id`
- `TenantContext`
- `TenantIgnore`
- `TenantLineHandler` 适配预留
- migration 中明确哪些表需要 tenant_id

### 2.3 数据权限

数据权限应该从权限模型中独立出来：

```text
Auth Permission：能不能访问某功能
Data Permission：访问功能后能看到哪些数据
```

### 2.4 基础设施模块

可以参考其基础设施模块思路，沉淀：

- 文件存储
- 消息通知
- 短信/邮件
- 支付适配
- 三方登录
- 工作流适配
- 监控运维

但 v0.1 只做接口预留。

## 3. 不建议照搬

### 3.1 不要第一版大而全

ruoyi-vue-pro 模块很多。如果 Synapse v0.1 直接追求完整覆盖，会导致：

- 代码量暴涨
- 权限边界不清
- 业务污染框架
- AI 协作失控
- 测试成本过高

### 3.2 不要把业务模块当框架模块

商城、ERP、MES、CRM、IoT 是业务或领域扩展，不是框架核心。

## 4. 对 Synapse 的决策

采用：

- MyBatis-Plus 路线
- 多租户字段预留
- 数据权限抽象
- 基础设施模块分层
- 企业扩展路线图

拒绝：

- 第一版引入大量业务模块
- 第一版引入完整工作流
- 第一版引入完整 SaaS 收费/套餐体系

## 5. Codex 使用建议

让 Codex 对标 ruoyi-vue-pro 时，重点提取：

- 系统表设计
- 多租户字段策略
- 数据权限枚举
- 代码生成器模板结构
- 基础设施模块边界

禁止让 Codex 一次性生成多个业务模块。
