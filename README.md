# Synapse Framework

面向 Java 企业级业务系统的通用技术基座。

Synapse Framework 不做业务系统、不做后台管理端、不做平台服务，而是沉淀可复用的框架契约、自动配置、上下文传播、安全基础、缓存并发能力、文件存储抽象、MQ 消息契约和审计事件模型。

## 快速了解

- **定位**：Java 企业应用技术支持框架。
- **阶段**：一阶段技术封板，二阶段进入边界固化与微服务技术基座规划。
- **JDK**：Java 21。
- **主栈**：Spring Boot 3.x / Maven 多模块。
- **Web 边界**：当前 `synapse-web` 仅 Servlet MVC，不包含 WebFlux / Gateway。
- **核心原则**：framework 只做技术能力，业务语义由消费方或 Synapse Platform 拥有。

## 边界声明

Synapse Framework 下所有 module 都必须是技术支持框架，不能是可启动服务，不能包含业务代码。

Framework 最多只能提供：

- 技术约束。
- 通用抽象。
- 通用模型。
- SPI / Port。
- 默认轻量实现。
- 自动装配。
- 上下文传播。
- 编码规范。
- 工具能力。
- starter 组合能力。

Framework 禁止提供：

- `@SpringBootApplication` 生产启动类。
- 业务 Controller。
- 业务 Service / Entity / Mapper / Repository。
- 业务数据库 migration。
- 用户、角色、菜单、组织、配置中心、文件中心、消息中心、任务中心等平台业务实现。
- Gateway / IAM / Message / File / Config / Task 等可启动服务。

可启动平台服务统一属于 Synapse Platform，例如 `synapse-gateway`、`synapse-iam`、`synapse-message-service`、`synapse-file-service`、`synapse-config-service`、`synapse-task-service`。

## 一阶段模块

当前已进入 root `pom.xml` reactor 的模块如下：

```text
synapse-framework
├── synapse-bom
├── synapse-core
├── synapse-web
├── synapse-data
├── synapse-cache
├── synapse-security
├── synapse-oauth2
├── synapse-audit
├── synapse-file
└── synapse-mq
```

| 模块 | 定位 | 手册 |
| --- | --- | --- |
| `synapse-bom` | 统一依赖版本管理 | [查看](docs/modules/synapse-bom.md) |
| `synapse-core` | 错误码、异常、ID、OperationContext 等核心契约 | [查看](docs/modules/synapse-core.md) |
| `synapse-web` | Servlet MVC 响应、异常处理、Filter 异常桥接 | [查看](docs/modules/synapse-web.md) |
| `synapse-data` | 数据层基础能力，当前聚焦 OperationContext 自动填充 | [查看](docs/modules/synapse-data.md) |
| `synapse-cache` | 缓存、锁、限流、幂等基础设施 | [查看](docs/modules/synapse-cache.md) |
| `synapse-security` | trusted-header、AuthenticatedUser、PermissionChecker、权限注解适配 | [查看](docs/modules/synapse-security.md) |
| `synapse-oauth2` | OAuth2 / JWT / JWK 技术能力 | [查看](docs/modules/synapse-oauth2.md) |
| `synapse-audit` | 审计事件契约 | [查看](docs/modules/synapse-audit.md) |
| `synapse-file` | 文件存储抽象与本地轻量实现 | [查看](docs/modules/synapse-file.md) |
| `synapse-mq` | MQ 消息外壳、发布/消费模板、SPI、上下文传播契约 | [查看](docs/modules/synapse-mq.md) |

模块手册索引：[docs/modules/README.md](docs/modules/README.md)

## 二阶段规划入口

二阶段规划文档位于 `docs/phase-2`：

| 文档 | 内容 |
| --- | --- |
| [00-Framework Boundary](docs/phase-2/00-framework-boundary.md) | Framework / Platform / Business Application 三层边界 |
| [01-Module Boundary](docs/phase-2/01-module-boundary.md) | 当前模块事实、二阶段目标模块形态、模块允许/禁止内容 |
| [02-Phase 2 Roadmap](docs/phase-2/02-phase-2-roadmap.md) | TASK-201 到 TASK-207 的任务拆分 |
| [03-Boundary Checklist](docs/phase-2/03-boundary-checklist.md) | 每个 TASK 执行前后的边界检查清单 |

注意：

- `synapse-webmvc`、`synapse-webflux`、`synapse-cloud`、`synapse-config`、`synapse-i18n`、`synapse-time`、`synapse-starter-*` 目前是二阶段规划模块。
- 未进入 root `pom.xml` reactor 前，不能把规划模块描述成已实现能力。
- `synapse-config` 在 Framework 中只能做配置抽象、配置客户端、运行时配置读取、解析、缓存、刷新扩展点；可启动配置服务属于 Platform。

## 快速开始

### 环境要求

```text
Java 21
Maven 3.9.x
```

### 构建与测试

```bash
mvn clean test
mvn validate
```

当前工作站 Maven 路径示例：

```bash
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -q clean test
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -q validate
```

### 业务项目引入方式

业务项目应按需引入模块，而不是一次性引入所有能力。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

示例：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-web</artifactId>
</dependency>
```

说明：当前 `synapse-web` 偏 Servlet MVC。Gateway / WebFlux 服务后续应使用二阶段规划中的 `synapse-webflux`，而不是依赖 `synapse-web`。

## 文档导航

| 文档 | 内容 |
| --- | --- |
| [01-项目定位与边界](docs/01-项目定位与边界.md) | 项目定位、一阶段边界、成功标准 |
| [02-总体架构设计](docs/02-总体架构设计.md) | 模块职责、依赖方向、设计原则 |
| [03-核心链路设计](docs/03-核心链路设计.md) | OperationContext、Web、Security、Data、MQ 等核心链路 |
| [04-技术复杂点](docs/04-技术复杂点.md) | 模块边界、异常链路、上下文传播、并发控制等风险点 |
| [05-待补充问题](docs/05-待补充问题.md) | 二阶段候选问题与冻结结论 |
| [06-基座与业务域边界设计](docs/06-基座与业务域边界设计.md) | framework、platform service、business application 的职责边界 |
| [07-工程结构与模块边界设计](docs/07-工程结构与模块边界设计.md) | 包结构、模块边界、禁止结构、测试规则 |
| [08-开发前技术决策记录](docs/08-开发前技术决策记录.md) | 一阶段关键技术决策 |
| [09-工程初始化实施清单](docs/09-工程初始化实施清单.md) | 开发前检查、自动配置检查、测试与验收命令 |
| [二阶段规划](docs/phase-2/02-phase-2-roadmap.md) | 二阶段任务拆分与执行顺序 |
| [模块使用手册](docs/modules/README.md) | 各模块接入方式、扩展点和边界说明 |

## 不做什么

Framework 明确不做：

- 不做业务 Controller。
- 不做业务 Entity / Mapper / migration。
- 不做生产启动应用。
- 不做后台管理端。
- 不做前端页面。
- 不做完整 IAM / RBAC / ABAC 平台。
- 不做 Gateway / WebFlux 可启动服务。
- 不做文件中心、消息中心、配置中心、审计中心、任务中心、集成中心。
- 不做租户和数据权限平台服务。

## 边界原则

```text
Business Application
  -> depends on Synapse Framework or Synapse Platform SDK / Client
  -> owns business model
  -> owns API
  -> owns database schema
  -> owns permission codes

Synapse Platform
  -> depends on Synapse Framework
  -> owns platform services
  -> owns gateway / iam / message / file / config / task runtime

Synapse Framework
  -> provides reusable technical foundation
  -> provides contracts and auto-configuration
  -> never depends on business application
  -> never becomes a runnable platform service
```

## 一阶段封板状态

当前一阶段已完成技术封板：

- 10 个 reactor 模块边界固定。
- `synapse-web` 已移除 WebFlux / Gateway 残留。
- `synapse-security` 不依赖 Spring Security Web / Config。
- `synapse-mq` 不包含真实 MQ / Redis 幂等 / DB / Outbox / 外部渠道 SDK 实现。
- `synapse-cache` 不包含业务缓存 key 或业务规则。
- `synapse-file` 不包含上传下载 API、附件表或文件权限业务。
- 本次模块重命名后需要重新执行全量测试与 validate。

## 许可证

当前未声明开源许可证。正式发布前需要补充 LICENSE。
