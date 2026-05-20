# Synapse Framework Phase 0-1 设计包

本目录用于启动 `synapse-framework` 的框架化沉淀工作，覆盖两个阶段：

- 阶段 0：开源后台框架对标分析
- 阶段 1：Synapse Framework 框架定位、架构规则、工程规范、AI 协作规范

## 目标

Synapse Framework 不是单一业务系统，也不是简单后台模板，而是面向企业内部应用的 Java 后台管理框架与快速开发底座。

第一版建议定位为：

> 单体优先、模块化设计、可演进到微服务、可被 AI Agent 规范协作的企业后台基础框架。

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
│   └── benchmark
├── skills
│   ├── synapse-architecture-review
│   ├── synapse-java-backend
│   ├── synapse-mybatis-plus-persistence
│   ├── synapse-security-rbac
│   ├── synapse-vue-admin
│   └── synapse-test-engineering
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
- Spring Boot 3.x
- Spring Security 6.x
- MyBatis-Plus 3.5.x
- Maven 多模块
- PostgreSQL 优先，MySQL 兼容预留
- Redis
- Flyway
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
>>>>>>> d8cdecd (first commit 第0阶段分析和第一阶段框架边界)
