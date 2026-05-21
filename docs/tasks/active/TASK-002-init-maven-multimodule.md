# TASK-20260520-002-init-maven-multimodule

## 1. Metadata

- Task ID: TASK-20260520-002
- Title: Initialize Maven multi-module skeleton
- Status: active
- Phase: Phase 2 - Maven Multi-Module Skeleton
- Owner Agent: backend_agent
- Related Agents: architect_agent, test_review_agent
- Related Skills: synapse-java-backend
- Related Issue:
- Related Branch: feature/TASK-20260520-002-init-maven-multimodule
- Related PR:
- Created At: 2026-05-20
- Updated At: 2026-05-20

## 2. Background

Synapse Framework 已完成 Phase 0/1 的定位、架构、规则、Agent 和任务追溯文件。当前需要进入 Phase 2，建立可编译、可测试、可扩展的 Maven 多模块工程骨架。

本任务只建立框架基础模块，不实现具体业务功能。

## 3. Goal

建立 Maven 多模块工程骨架，包括：

- root parent pom
- synapse-bom
- synapse-common
- synapse-web
- synapse-data
- synapse-security
- synapse-audit
- synapse-starter
- scripts/verify.sh
- 每个模块的最小测试

## 4. Non-Goals

本次不做：

- 用户管理
- 角色管理
- 菜单管理
- 登录认证
- JWT
- Refresh Token
- 数据权限真实实现
- 多租户真实实现
- 代码生成器
- 前端页面
- 数据库 migration
- Docker / K8s 部署

## 5. Scope

### 5.1 Allowed Changes

允许修改：

- pom.xml
- synapse-bom/**
- synapse-common/**
- synapse-web/**
- synapse-data/**
- synapse-security/**
- synapse-audit/**
- synapse-starter/**
- scripts/**
- docs/tasks/**

### 5.2 Forbidden Changes

禁止修改：

- 前端代码
- 业务模块代码
- 数据库 migration
- AGENTS.md，除非只补充必要说明
- skills/**，除非任务明确要求

## 6. Required Reading

执行前必须读取：

- AGENTS.md
- docs/00-positioning.md
- docs/01-architecture.md
- docs/02-module-boundary.md
- docs/03-package-rules.md
- docs/04-database-rules.md
- docs/05-api-rules.md
- docs/06-security-rules.md
- docs/07-test-rules.md
- docs/08-ai-development-rules.md
- skills/synapse-java-backend/SKILL.md

## 7. Implementation Requirements

1. 使用 Java 21。
2. 使用 Maven 多模块。
3. 根 `pom.xml` 只做 parent 和 modules 聚合。
4. `synapse-bom` 只做 dependencyManagement，不写 Java 代码。
5. 所有模块 groupId 使用 `com.indigo.synapse`。
6. 所有 Java 包名以 `com.indigo.synapse` 开头。
7. 每个模块必须至少有一个最小单元测试。
8. `scripts/verify.sh` 必须执行 Maven 测试。
9. 不允许引入业务 Controller。
10. 不允许创建业务表。
11. 不允许复制任何开源框架代码。

## 8. Architecture Constraints

1. `synapse-common` 不依赖其他 Synapse 模块。
2. `synapse-web` 可以依赖 `synapse-common`。
3. `synapse-data` 可以依赖 `synapse-common`。
4. `synapse-security` 可以依赖 `synapse-common`。
5. `synapse-audit` 可以依赖 `synapse-common`。
6. `synapse-starter` 可以依赖 common/web/data/security/audit。
7. 不允许循环依赖。
8. 不允许业务能力进入 common。
9. 不允许 Domain Model 依赖 MyBatis-Plus。
10. 不允许 Controller 直接依赖 Mapper。

## 9. Acceptance Criteria

- [ ] `mvn test` 通过
- [ ] `scripts/verify.sh` 通过
- [ ] 所有模块可以被 Maven 正确识别
- [ ] 所有模块至少有一个测试
- [ ] 无业务功能实现
- [ ] 无数据库 migration
- [ ] 无前端变更
- [ ] 无循环依赖
- [ ] Run Log 已记录

## 10. Suggested Execution Plan

1. 创建 root `pom.xml`。
2. 创建 `synapse-bom`。
3. 创建 common/web/data/security/audit/starter 模块。
4. 添加最小 Java 类和测试。
5. 添加 `scripts/verify.sh`。
6. 执行 `mvn test`。
7. 创建 Run Log。

## 11. Result Summary

任务完成后填写。

## 12. Change Log

| Time | Actor | Change |
|---|---|---|
| 2026-05-20 | Chris | Create Phase 2 Maven multi-module skeleton task |