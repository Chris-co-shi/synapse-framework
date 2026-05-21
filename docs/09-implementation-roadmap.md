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

## 阶段 2：技术基座骨架

目标：搭建 Maven 多模块技术基座工程。

模块：

```text
synapse-bom
synapse-common
synapse-web
synapse-data
synapse-cache
synapse-security
synapse-audit
synapse-starter
```

完成标准：

- `mvn test` 通过
- 统一响应可用
- 统一异常可用
- 技术基线写入 BOM
- 模块完成后有对应 `skills/<module>/SKILL.md`

## 阶段 3：Core Foundation

目标：完成 Web、Data、Cache、Security、Audit 的可复用基础能力。

交付物：

```text
Web: MVC/WebFlux 统一响应、异常、分页、Trace、OpenAPI
Data: MyBatis-Plus 3.5.9、动态数据源、数据库方言、Flyway
Cache: Redis、Lua、可重入锁、滑动窗口限流
Security: OAuth2 Authorization Server、Resource Server、JWT、JWK
Audit: 审计事件、审计 Port、操作日志注解
```

完成标准：

- 每个模块测试通过
- 关键模块根目录 `mvn clean test` 通过
- 每个模块完成后沉淀 `SKILL.md`

## 阶段 4：Starter 与示例应用

目标：让业务项目通过 starter 接入技术基座。

交付物：

- 自动配置
- 条件装配
- 默认 properties
- 示例应用
- 多数据源、Redis 锁、限流、OAuth2 示例

完成标准：

- 示例应用可启动
- starter 引入后基础能力可用
- 示例测试通过

## 阶段 5：IAM 验证模块

目标：用 IAM/Auth/RBAC 验证技术基座。

完成标准：

- OAuth2 登录授权链路可用
- 资源服务器权限保护可用
- 用户、客户端、角色权限最小模型可用
- 验证 Data/Cache/Security/Audit/Web 能支撑真实模块

## 阶段 6：基础系统模块

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

## 阶段 7：代码生成器 v0.1

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

## 阶段 8：框架验证业务模块

目标：用一个小型业务模块验证框架通用性。

建议模块：

- 资产管理 asset
- 工单管理 work-order
- 客户管理 customer

完成标准：

- 代码生成器生成基础代码
- 人工补业务规则
- 权限/审计/字典/分页均可复用

## 阶段 9：生产化增强

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
