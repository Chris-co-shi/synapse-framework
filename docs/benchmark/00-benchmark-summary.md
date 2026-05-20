# 阶段 0：开源后台框架对标总览

## 1. 对标目标

本阶段不是为了复制某个开源框架，而是为了抽取通用后台框架的稳定能力模型。

对标项目：

| 项目 | 参考价值 | 不建议照搬的部分 |
|---|---|---|
| RuoYi-Vue | 最小后台管理骨架、权限菜单、代码生成 | 老技术栈、包结构、部分工具式写法 |
| eladmin | 后台模块边界、RBAC、数据权限、代码生成、运维模块 | JPA 持久化路线、旧前端栈 |
| ruoyi-vue-pro | 企业扩展能力、多租户、数据权限、工作流、支付、商城等模块化扩展 | 大而全，一开始照搬会失控 |
| JeecgBoot | 低代码、在线表单、报表、代码生成、插件化、AI/低代码融合 | 魔法封装重、学习成本高、第一版不宜全量复刻 |
| Spring Boot Admin | 运维监控台、Actuator 管理界面 | 不是业务后台框架，只能补充运维能力 |
| SpringBlade | 企业 SaaS、多租户、Spring Boot/Spring Cloud 双架构 | 体系重，商业化痕迹强，不能成为 v0.1 母体 |

## 2. 结论

Synapse Framework 第一版不应该 Fork 任意一个项目，而应该采用：

```text
RuoYi-Vue 的最小后台骨架
+ eladmin 的后台模块边界
+ ruoyi-vue-pro 的企业扩展预留
+ JeecgBoot 的代码生成/元数据思想
+ Spring Boot Admin 的监控思路
= Synapse Admin Framework v0.1
```

## 3. 能直接吸收的能力

### 3.1 系统管理能力

- 用户管理
- 角色管理
- 菜单管理
- 权限管理
- 部门管理
- 岗位管理
- 字典管理
- 参数配置
- 通知公告
- 操作日志
- 登录日志
- 在线用户
- 文件管理
- 定时任务

### 3.2 框架基础能力

- 统一响应结构
- 统一异常处理
- 统一错误码
- 统一分页模型
- 统一审计字段
- 统一租户字段预留
- 统一逻辑删除
- 统一乐观锁
- 统一登录上下文
- 统一操作日志注解
- 统一权限校验注解

### 3.3 生产化能力

- Flyway migration
- OpenAPI 文档
- Actuator 健康检查
- Spring Boot Admin 集成预留
- Redis 缓存
- 幂等控制
- 限流预留
- 审计日志
- CI/CD 模板
- Dockerfile
- Helm Chart 预留

## 4. 不应该直接吸收的能力

第一版不要做：

- 完整在线表单
- 完整低代码平台
- 完整工作流平台
- 完整微服务治理
- 完整 BI 报表平台
- 完整插件市场
- 完整 AI 应用平台
- 完整商城、CRM、ERP、MES 业务模块

这些应该作为 v0.3 之后的扩展模块。

## 5. Synapse v0.1 建议范围

```text
synapse-framework
├── synapse-bom
├── synapse-common
├── synapse-web
├── synapse-data
├── synapse-security
├── synapse-audit
├── synapse-tenant
├── synapse-cache
├── synapse-codegen
├── synapse-admin-api
└── synapse-admin-ui
```

## 6. 对标来源

- RuoYi-Vue: https://github.com/yangzongzhuan/RuoYi-Vue
- eladmin: https://github.com/elunez/eladmin
- ruoyi-vue-pro: https://github.com/YunaiV/ruoyi-vue-pro
- JeecgBoot: https://github.com/jeecgboot/JeecgBoot
- Spring Boot Admin: https://github.com/codecentric/spring-boot-admin
- SpringBlade: https://github.com/chillzhuang/SpringBlade
