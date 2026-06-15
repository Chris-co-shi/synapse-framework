# 03-Boundary Checklist

本文档用于二阶段每个 TASK 执行前后进行边界检查。它不是一次性检查清单，而是后续 Codex、Agent 和人工开发的常规入口。

## 1. 执行前检查

每个 TASK 执行前必须回答：

1. 本次任务是否属于当前 TASK 范围？
2. 是否会新增 module？如果会，是否已经有边界文档？
3. 是否会修改 POM？如果会，是否属于当前 TASK 允许范围？
4. 是否会新增生产依赖？为什么必须新增？
5. 是否会新增 AutoConfiguration？是否有条件装配保护？
6. 是否会新增 Controller？如果是生产 Controller，立即停止。
7. 是否会新增 `@SpringBootApplication`？如果是生产启动类，立即停止。
8. 是否会新增业务 Entity / Mapper / Repository / Service？如果是，立即停止。
9. 是否会新增数据库 migration？如果是业务表，立即停止。
10. 是否会把 Platform 能力放进 Framework？如果是，立即停止。

## 2. 执行后检查

每个 TASK 完成后必须确认：

- 没有新增生产启动类。
- 没有新增业务 Controller。
- 没有新增业务 Entity / Mapper / Repository / Service。
- 没有新增业务数据库 migration。
- 没有把 config 做成 config-service。
- 没有把 file 做成 file-service。
- 没有把 mq 做成 message-service。
- 没有把 oauth2 做成 IAM。
- 没有把 webflux 做成 gateway。
- 没有在 Framework 中引入 Platform 业务概念。
- 没有引入不必要的强依赖。
- 文档中的“当前事实”和“后续规划”表述清晰。

## 3. 推荐搜索命令

### 3.1 启动类检查

```bash
rg -n "@SpringBootApplication|SpringApplication\.run" .
```

判断规则：

- `src/main/java` 中出现生产启动类通常违规。
- `src/test/java` 中测试 fixture 出现启动类不一定违规，但必须仅用于测试。

### 3.2 Controller 检查

```bash
rg -n "@RestController\b|@Controller\b|@RequestMapping\b|@GetMapping\b|@PostMapping\b|@PutMapping\b|@DeleteMapping\b" '*/src/main/java'
```

判断规则：

- 业务 Controller 违规。
- `@RestControllerAdvice` 属于异常基础设施，不等于业务 Controller。
- 测试 fixture 中的 Controller 不等于生产 Controller。
- 技术型 WebFlux handler / filter 不等于业务 Controller，但必须确认没有业务 API 语义。

### 3.3 业务持久化检查

```bash
rg -n "@TableName\b|BaseMapper\b|IService\b|ServiceImpl\b|CREATE TABLE|create table" '*/src/main'
```

判断规则：

- 业务 Entity / Mapper / Service 违规。
- 技术型 `BaseEntity` / `AuditableEntity` / `VersionedEntity` 不等于业务 Entity。
- Port / SPI 不等于业务 Repository。
- framework 不应该引入用户、角色、菜单、组织、配置项、文件记录、消息模板等业务表。

### 3.4 平台服务关键词检查

```bash
rg -n "file-service|message-service|config-service|audit-service|task-service|iam-service|用户中心|配置中心|文件中心|消息中心|审计中心|任务中心|国际化资源中心" README.md AGENTS.md docs
```

判断规则：

- 文档中用于说明“这些属于 Platform”是合理命中。
- 如果代码或模块描述把这些写成 framework 已实现能力，则违规。

### 3.5 WebMVC / WebFlux 依赖检查

```bash
rg -n "spring-webmvc|DispatcherServlet|jakarta.servlet|ServerWebExchange|WebFilter|reactor.core" .
```

判断规则：

- WebMVC 模块可以依赖 Servlet / `spring-webmvc`。
- WebFlux 模块不得依赖 `spring-webmvc`。
- Gateway 不应依赖 WebMVC 模块。
- TASK-202 后正式模块为 `synapse-webmvc` 和 `synapse-webflux`，不再保留 `synapse-web`。

TASK-202 后建议补充执行：

```bash
rg -n "spring-webmvc|jakarta.servlet|DispatcherServlet" synapse-webflux
rg -n "spring-webflux|ServerWebExchange|WebFilter|reactor.core" synapse-webmvc
```

### 3.6 OAuth2 / IAM 边界检查

```bash
rg -n "AuthorizationServer|RegisteredClient|OAuth2AuthorizationService|login|client management|用户|角色|菜单|授权后台" synapse-oauth2 synapse-security docs AGENTS.md README.md
```

判断规则：

- 二阶段 `synapse-oauth2` 只允许 JWT / JWK / Token / Resource Server 辅助能力。
- Authorization Server 实现、登录、客户端管理、用户认证、授权后台属于 Platform `synapse-iam`。
- 文档中描述禁止事项属于合理命中。

### 3.7 Config 边界检查

```bash
rg -n "ConfigController|配置发布|配置审批|配置中心|config-service|CREATE TABLE.*config|config_item" .
```

判断规则：

- `ConfigClient` / `ConfigResolver` / `ConfigParser` 属于 framework 抽象。
- `ConfigController`、配置发布、审批、配置中心后台属于 Platform。
- 文档中说明边界属于合理命中。

### 3.8 File 边界检查

```bash
rg -n "FileController|Attachment|附件表|文件中心|file-service|preview|watermark|OCR" .
```

判断规则：

- `FileStorageClient`、文件存储 SPI、URL 签名抽象属于 framework。
- 文件管理 API、附件业务表、预览、转码、水印、OCR 属于 Platform 或业务应用。

### 3.9 MQ / Message 边界检查

```bash
rg -n "站内信|短信|邮件|消息模板|message-service|MessageTemplate|Notification|Inbox" .
```

判断规则：

- MQ envelope、publisher、consumer、header、idempotency key 属于 framework。
- 站内信、短信、邮件、消息模板、消息中心属于 Platform。

### 3.10 Audit 边界检查

```bash
rg -n "AuditController|审计报表|审计中心|audit-service|AuditReport|AuditQuery" .
```

判断规则：

- `AuditEvent`、`AuditLogPort`、审计事件发布扩展点属于 framework。
- 审计查询、审计报表、审计中心后台属于 Platform。

### 3.11 Cloud / Feign 边界检查

```bash
rg -n "spring-cloud-starter-gateway|RouteLocator|GatewayFilter|GlobalFilter|nacos|seata|rocketmq" .
rg -n "synapse-webmvc|synapse-webflux|synapse-security|synapse-mq" synapse-cloud || true
rg -n "roles|permissions|menu|organization|raw token|password|credential|Authorization" synapse-cloud docs/phase-2/04-cloud-context-propagation.md || true
rg -n "IAM|登录认证|业务鉴权|注册中心|配置中心|服务治理后台" synapse-cloud docs/phase-2/04-cloud-context-propagation.md || true
```

判断规则：

- `synapse-cloud` 只能做 Spring Cloud / OpenFeign / 服务间调用技术适配。
- `synapse-cloud` 不得依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`。
- `synapse-cloud` 不得引入 Gateway、Nacos、Seata、RocketMQ。
- `synapse-cloud` 不得实现 IAM、登录认证、业务鉴权、注册中心、配置中心或服务治理后台。
- 服务间 Header 禁止传播 roles、permissions、menu codes、organization tree、raw token、password、credential 和 business data。
- 文档中说明这些能力“禁止进入 framework”属于合理命中。

## 4. 合理命中说明

以下命中不一定违规，需要结合位置和语义判断：

| 命中 | 是否一定违规 | 判断方式 |
| --- | --- | --- |
| `@RestControllerAdvice` | 否 | 全局异常基础设施，允许存在于 webmvc 模块 |
| 测试 Controller | 否 | 仅允许在 `src/test` fixture 中出现 |
| 技术型 `BaseEntity` | 否 | 不包含业务字段和业务表语义时可以作为 data 技术基类 |
| `Port` / `SPI` | 否 | 技术扩展点允许存在 |
| `Service` 字样 | 不一定 | 业务 Service 禁止，技术型 service/helper/template 需要结合语义判断 |
| Platform 关键词 | 不一定 | 文档中说明“属于 Platform”合理，代码中实现则需警惕 |
| `CREATE TABLE` | 不一定 | 技术测试表可能合理，业务表不允许 |
| Cloud 禁止关键词 | 不一定 | 文档中说明禁止项合理，`synapse-cloud` 生产代码中实现或依赖通常违规 |

## 5. 任务级必检项

### TASK-201

- 只允许文档修改。
- 不修改 POM。
- 不修改 Java。
- 不新增 module。
- 不重命名 module。

### TASK-202

- 可以处理 WebMVC / WebFlux 拆分。
- 必须确认 WebFlux 不引入 `spring-webmvc`。
- 必须确认 `synapse-webflux` 不是 gateway 服务。

### TASK-203

- 可以新增 `synapse-cloud`。
- 不得引入注册中心服务或配置中心服务。
- Feign / LoadBalancer 只作为技术调用能力。
- TASK-203-A 只允许文档冻结，不新增 module、不修改 POM、不新增 Java。
- TASK-203-B 才允许新增 `synapse-cloud` module 和修改 root POM / BOM。
- `synapse-cloud` 不得依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`。
- `synapse-cloud` 不得引入 Gateway、Nacos、Seata、RocketMQ。
- `synapse-cloud` 不得实现 IAM、登录认证或业务鉴权。
- 服务间 Header 不得传播 roles / permissions / menu / raw token。
- RequestInterceptor 优先于 ErrorDecoder。
- 服务间签名只作为扩展点，不得做完整认证体系。

### TASK-204

- 可以增强 OperationContext。
- 必须处理 ThreadLocal 清理和上下文恢复。
- 不得默认把缺失上下文伪装成不可追溯的 system actor。

### TASK-205

- 可以新增 `synapse-time`、`synapse-config`、`synapse-i18n`。
- `synapse-time` 独立模块，不并入 core。
- `synapse-config` 不得做 config-service。
- `synapse-i18n` 不得做 resource center。

### TASK-206

- 复查 `synapse-mq`、`synapse-file`、`synapse-audit`、`synapse-oauth2`。
- 不得为了闭环写入平台业务实现。

### TASK-207

- starter 后置。
- examples 不得变成生产启动服务。
- 文档必须区分当前事实和后续规划。

## 6. 最小验收命令

```bash
rg -n "@SpringBootApplication|SpringApplication\.run" .
rg -n "@RestController\b|@Controller\b|@RequestMapping\b|@GetMapping\b|@PostMapping\b" '*/src/main/java'
rg -n "@TableName\b|BaseMapper\b|IService\b|ServiceImpl\b|CREATE TABLE|create table" '*/src/main'
rg -n "file-service|message-service|config-service|audit-service|task-service|用户中心|配置中心|文件中心|消息中心|审计中心" README.md AGENTS.md docs
rg -n "spring-cloud-starter-gateway|nacos|seata|rocketmq" .
rg -n "synapse-webmvc|synapse-webflux" synapse-cloud || true
git diff --check
```

说明：

- 命中后必须人工判断是否为合理命中。
- `git diff --check` 必须通过。
- 文档变更任务不要求执行 `mvn test`，但代码变更任务必须执行对应测试。
