# Synapse Framework Phase 0-1 设计包

本目录用于启动 `synapse-framework` 的框架化沉淀工作，覆盖两个阶段：

- 阶段 0：开源后台框架对标分析
- 阶段 1：Synapse Framework 框架定位、架构规则、工程规范、AI 协作规范

## 目标

Synapse Framework 不是单一业务系统，也不是简单后台模板，而是面向企业内部应用的 Java 通用技术基座与后台快速开发底座。

第一版建议定位为：

> 单体优先、模块化设计、可演进到微服务、可被 AI Agent 规范协作的企业技术基座。

## 推荐目录

```text
synapse-framework
├── AGENTS.md
├── README.md
├── docs
│   ├── 00-positioning.md
│   ├── 01-architecture.md
│   ├── 02-module-boundary.md
│   ├── 03-package-rules.md
│   ├── 04-database-rules.md
│   ├── 05-api-rules.md
│   ├── 06-security-rules.md
│   ├── 07-test-rules.md
│   ├── 08-ai-development-rules.md
│   ├── 09-implementation-roadmap.md
│   ├── 10-technical-foundation-baseline.md
│   └── benchmark
├── skills
│   ├── synapse-core
│   ├── synapse-web
│   ├── synapse-data
│   ├── synapse-cache
│   ├── synapse-security
│   ├── synapse-audit
│   ├── synapse-starter
│   └── synapse-example
└── templates
    └── codex-task-template.md
```

## 使用方式

1. 将本包内容复制到你的 `synapse-framework` 仓库根目录。
2. 先让 Codex 读取 `AGENTS.md`、`docs/00-positioning.md`、`docs/01-architecture.md`、`docs/03-package-rules.md`、`docs/08-ai-development-rules.md`。
3. 每次开发前，按 `templates/codex-task-template.md` 填写任务。
4. 每个模块实现前必须先补充设计说明，再写代码。
5. 每次实现后必须输出修改文件、测试结果、风险点。

## 当前推荐技术基线

- Java 21
- Spring Boot 3.5.14
- Spring Security 6.5.x
- OAuth2 Authorization Server + Resource Server
- JWT + JWK
- MyBatis-Plus 3.5.9
- dynamic-datasource Spring Boot 3 starter
- Maven 3.9.0 多模块，当前工作站使用 `/Users/sxc/Documents/tool/apache-maven-3.9.0`
- 数据库不绑定具体厂商，通过方言适配层支持切换
- Redis / Spring Data Redis / Lettuce
- Redis + Lua 可重入分布式锁
- Redis + Lua 滑动窗口限流
- Flyway
- H2 + Testcontainers
- springdoc OpenAPI 2.8.x
- Lombok + MapStruct
- Vue 3 + TypeScript + Vite
- Element Plus 或 Naive UI
- JUnit 5 + Mockito + Spring Boot Test

## 第一版不要做什么

- 不做完整低代码平台
- 不做完整工作流平台
- 不做完整微服务治理平台
- 不做插件市场
- 不做 AI 应用平台
- 不复制开源项目代码
- 不把业务系统代码混入框架核心

## 模块最佳实践沉淀规则

每完成一个模块并通过测试后，必须沉淀：

```text
skills/<module-name>/SKILL.md
```

`SKILL.md` 只记录可复用最佳实践、边界、实现模式和测试要求，不写过程日志。

## 第一版必须做什么

- 统一认证登录
- 用户、角色、菜单、权限
- 动态路由
- 数据权限预留
- 多租户预留
- 字典、参数、审计日志
- 统一异常、响应、分页、错误码
- MyBatis-Plus 持久化规范
- 代码生成器最小闭环
- AI 协作规范与 Skills
