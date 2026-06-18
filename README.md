# Synapse Framework

面向 Java 企业级业务系统的通用技术基座。

Synapse Framework 不做业务系统、不做后台管理端、不做平台服务，而是沉淀可复用的框架契约、自动配置、上下文传播、安全基础、缓存并发能力、文件存储抽象、MQ 消息契约和审计事件模型。

## 快速了解

- **定位**：Java 企业应用技术支持框架。
- **阶段**：二阶段已完成 WebMVC / WebFlux 技术栈拆分，并新增 Cloud / Feign 上下文传播技术支撑。
- **JDK**：Java 21。
- **主栈**：Spring Boot 3.x / Maven 多模块。
- **Web 边界**：`synapse-webmvc` 支撑 Servlet MVC；`synapse-webflux` 支撑 WebFlux，不是 Gateway 服务。
- **核心原则**：framework 只做技术能力，业务语义由消费方或 Synapse Platform 拥有。
- **交付约定**：不提供 starter 聚合包，不提供 demo / example / sample application。

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
- 测试与文档沉淀。
- Skill 最佳实践。

Framework 禁止提供：

- `@SpringBootApplication` 生产启动类。
- 业务 Controller。
- 业务 Service / Entity / Mapper / Repository。
- 业务数据库 migration。
- starter 聚合包。
- demo / example / sample application。
- 用户、角色、菜单、组织、配置中心、文件中心、消息中心、任务中心等平台业务实现。
- Gateway / IAM / Message / File / Config / Task 等可启动服务。

可启动平台服务统一属于 Synapse Platform，例如 `synapse-gateway`、`synapse-iam`、`synapse-message-service`、`synapse-file-service`、`synapse-config-service`、`synapse-task-service`。

## 当前模块

当前已进入 root `pom.xml` reactor 的模块如下：

```text
synapse-framework
├── synapse-bom
├── synapse-core
├── synapse-webmvc
├── synapse-webflux
├── synapse-cloud
├── synapse-time
├── synapse-config
├── synapse-i18n
├── synapse-data
├── synapse-cache
├── synapse-security
├── synapse-oauth2-core
├── synapse-oauth2-authorization-server-support
├── synapse-oauth2-resource-server-webmvc
├── synapse-oauth2-resource-server-webflux
├── synapse-audit
├── synapse-file
└── synapse-mq
```

| 模块 | 定位 | 手册 |
| --- | --- | --- |
| `synapse-bom` | 统一依赖版本管理 | [查看](docs/modules/synapse-bom.md) |
| `synapse-core` | 错误码、异常、ID、OperationContext 等核心契约 | [查看](docs/modules/synapse-core.md) |
| `synapse-webmvc` | Servlet MVC 响应、异常处理、Filter 异常桥接 | [查看](docs/modules/synapse-webmvc.md) |
| `synapse-webflux` | WebFlux Trace、异常响应、Reactor Context / OperationContext 恢复 | [查看](docs/modules/synapse-webflux.md) |
| `synapse-cloud` | Spring Cloud / OpenFeign 服务间调用上下文传播 | [查看](docs/modules/synapse-cloud.md) |
| `synapse-time` | 时间和时区技术支撑，提供 UTC 查询范围转换 | [查看](docs/modules/synapse-time.md) |
| `synapse-config` | 配置抽象、运行时读取和类型解析，不是配置中心 | [查看](docs/modules/synapse-config.md) |
| `synapse-i18n` | 国际化消息解析抽象，不是资源中心 | [查看](docs/modules/synapse-i18n.md) |
| `synapse-data` | 数据层基础能力，当前聚焦 OperationContext 自动填充 | [查看](docs/modules/synapse-data.md) |
| `synapse-cache` | 缓存、锁、限流、幂等基础设施 | [查看](docs/modules/synapse-cache.md) |
| `synapse-security` | Web 无关安全主体、AuthenticatedUser/Client、PermissionChecker、权限注解适配 | [查看](docs/modules/synapse-security.md) |
| `synapse-oauth2-core` | JWT claim、token、validator、denylist 和 BearerTokenProvider 契约 | [查看](docs/modules/synapse-oauth2-core.md) |
| `synapse-oauth2-authorization-server-support` | JWT 签发、RSAKey、JWKSource、JwtEncoder 技术支持 | [查看](docs/modules/synapse-oauth2-authorization-server-support.md) |
| `synapse-oauth2-resource-server-webmvc` | Servlet OAuth2 Resource Server 技术适配 | [查看](docs/modules/synapse-oauth2-resource-server-webmvc.md) |
| `synapse-oauth2-resource-server-webflux` | Reactive OAuth2 Resource Server 技术适配 | [查看](docs/modules/synapse-oauth2-resource-server-webflux.md) |
| `synapse-audit` | 审计事件契约 | [查看](docs/modules/synapse-audit.md) |
| `synapse-file` | 文件存储抽象与本地轻量实现 | [查看](docs/modules/synapse-file.md) |
| `synapse-mq` | MQ 消息外壳、发布/消费模板、SPI、上下文传播契约 | [查看](docs/modules/synapse-mq.md) |

模块手册索引：[docs/modules/README.md](docs/modules/README.md)

模块 Skill 索引：[skills/README.md](skills/README.md)

## 学习入口

如果需要重新接管代码、按设计理解模块或开始手写练习，请从以下文档开始：

| 文档 | 内容 |
| --- | --- |
| [学习路径索引](docs/learning/README.md) | 推荐阅读顺序、学习方法和掌握标准 |
| [Framework 架构阅读指南](docs/learning/01-framework-architecture-reading-guide.md) | 三层边界、模块地图、依赖方向和源码阅读方式 |
| [Security 与 OAuth2 请求链路](docs/learning/02-security-oauth2-request-flow.md) | Bearer Token 到 SecurityContext、OperationContext 和权限检查的完整链路 |

## 二阶段规划入口

二阶段规划文档位于 `docs/phase-2`：

| 文档 | 内容 |
| --- | --- |
| [00-Framework Boundary](docs/phase-2/00-framework-boundary.md) | Framework / Platform / Business Application 三层边界 |
| [01-Module Boundary](docs/phase-2/01-module-boundary.md) | 当前模块事实、二阶段目标模块形态、模块允许/禁止内容 |
| [02-Phase 2 Roadmap](docs/phase-2/02-phase-2-roadmap.md) | TASK-201 到 TASK-207 的任务拆分 |
| [03-Boundary Checklist](docs/phase-2/03-boundary-checklist.md) | 每个 TASK 执行前后的边界检查清单 |
| [04-Cloud Context Propagation](docs/phase-2/04-cloud-context-propagation.md) | `synapse-cloud` 服务间调用 Header 契约与 Feign 适配边界 |

注意：

- `synapse-webmvc`、`synapse-webflux`、`synapse-cloud`、`synapse-time`、`synapse-config`、`synapse-i18n` 是当前已实现模块。
- 本项目不规划、不创建 `synapse-starter-*`。
- 本项目不规划、不创建 demo / example / sample application。
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

### Configuration Metadata

所有公开 `@ConfigurationProperties` 都必须生成 Spring Boot Configuration Metadata。发布前需要确认对应 jar 中包含：

```text
META-INF/spring-configuration-metadata.json
```

该 metadata 用于消费方在 IntelliJ IDEA 等 IDE 中获得 `synapse.*` 配置前缀、属性名、类型、默认值、说明和候选值补全。新增配置项时必须同步补充字段 Javadoc，并验证 metadata 进入最终 jar。

### 业务项目引入方式

业务项目应按需直接引入具体 module，而不是通过 starter 或 demo 应用间接引入。

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

Servlet MVC 服务引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>
```

WebFlux 服务引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webflux</artifactId>
</dependency>
```

说明：Gateway 可启动服务属于 Synapse Platform。Platform Gateway 可以引用 `synapse-webflux` 的技术支撑能力，但 Gateway 路由、鉴权业务和启动服务不进入 Framework。

OpenFeign 服务间调用引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-cloud</artifactId>
</dependency>
```

说明：`synapse-cloud` 只提供 Feign 上下文传播、远程错误解码和条件自动配置，不提供 Gateway、注册中心、配置中心、服务治理后台或 IAM。

## 文档导航

| 文档 | 内容 |
| --- | --- |
| [学习路径](docs/learning/README.md) | 面向代码接管和手写练习的阅读顺序 |
| [01-项目定位与边界](docs/01-项目定位与边界.md) | 项目定位、一阶段边界、成功标准 |
| [02-总体架构设计](docs/02-总体架构设计.md) | 模块职责、依赖方向、设计原则 |
| [03-核心链路设计](docs/03-核心链路设计.md) | OperationContext、Web、Security、Data、MQ 等核心链路 |
| [04-技术复杂点](docs/04-技术复杂点.md) | 模块边界、异常链路、上下文传播、并发控制等风险点 |
| [06-待补充问题](docs/06-待补充问题.md) | 二阶段候选问题与冻结结论 |
| [二阶段规划](docs/phase-2/02-phase-2-roadmap.md) | 二阶段任务拆分与执行顺序 |
| [模块使用手册](docs/modules/README.md) | 各模块接入方式、扩展点和边界说明 |

## 不做什么

Framework 明确不做：

- 不做业务 Controller。
- 不做业务 Entity / Mapper / migration。
- 不做生产启动应用。
- 不做 starter 聚合包。
- 不做 demo / example / sample application。
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
  -> never provides starter/demo/example/sample application
```

## 当前状态

- `synapse-webmvc` 承接原 Servlet MVC Web 能力。
- `synapse-webflux` 提供 WebFlux 最小技术闭环，不包含 Gateway 服务。
- `synapse-cloud` 提供 OpenFeign 出站 OperationContext Header 编码、RequestInterceptor、ErrorDecoder 和自动配置，不包含 Gateway、Nacos、Seata、RocketMQ、IAM 或业务鉴权。
- `synapse-security` 是 Web 无关安全基础模块；认证主体由 OAuth2 Resource Server 适配模块从经过验证的 Bearer Token 建立。
- OAuth2 已拆分为 core、authorization-server-support、resource-server-webmvc、resource-server-webflux；旧 `synapse-oauth2` 不再是正式 reactor module。
- `synapse-web` 不再作为正式 reactor module 保留。
- `synapse-security` 不依赖 Spring Security Web / Config。
- `synapse-mq` 不包含真实 MQ / Redis 幂等 / DB / Outbox / 外部渠道 SDK 实现。
- `synapse-cache` 不包含业务缓存 key 或业务规则。
- `synapse-file` 不包含上传下载 API、附件表或文件权限业务。
- Framework 不提供 starter，也不提供 demo / example / sample application。

## 许可证

当前未声明开源许可证。正式发布前需要补充 LICENSE。
