# Synapse Framework AI 协作规范

本文件是本仓库的 AI Agent 总入口。任何 Codex、代码 Agent、Review Agent 在修改本仓库前必须先读取本文件。

本文件只保留长期有效的协作规则。具体模块边界、任务路线和实现细节以 `README.md`、`docs/phase-2/*`、`docs/modules/*` 和已存在的 `skills/*/SKILL.md` 为准。

## 1. 项目定位

Synapse Framework 是面向 Java 企业应用的通用技术支持框架。

它只交付：

- module。
- 技术抽象。
- SPI / Port / Adapter。
- 默认轻量实现。
- Spring Boot AutoConfiguration。
- 上下文传播能力。
- 测试。
- 文档。
- Skill 最佳实践。

它不交付：

- 可启动平台服务。
- 业务系统。
- 后台管理端。
- 前端页面。
- starter 聚合包。
- demo / example / sample application。

## 2. 当前正式 reactor modules

当前正式模块以根 `pom.xml` 的 `<modules>` 为准：

```text
synapse-bom
synapse-core
synapse-webmvc
synapse-webflux
synapse-cloud
synapse-time
synapse-config
synapse-i18n
synapse-data
synapse-cache
synapse-security
synapse-security-webmvc
synapse-oauth2-core
synapse-oauth2-authorization-server-support
synapse-oauth2-resource-server-webmvc
synapse-oauth2-resource-server-webflux
synapse-audit
synapse-file
synapse-mq
```

固定结论：

- `synapse-web` 已拆分为 `synapse-webmvc` 和 `synapse-webflux`，不得恢复为正式模块。
- `synapse-message` 已更名为 `synapse-mq`，不得继续使用旧名称描述正式模块。
- `synapse-config`、`synapse-i18n`、`synapse-time` 已在 TASK-205 进入 reactor，必须按当前已实现技术模块描述。
- `synapse-oauth2` 已拆分为 `synapse-oauth2-core`、`synapse-oauth2-authorization-server-support`、`synapse-oauth2-resource-server-webmvc`、`synapse-oauth2-resource-server-webflux`，不得继续作为正式 reactor module 描述。
- `synapse-task`、`synapse-tenant`、`synapse-data-permission` 若存在目录，也只视为暂存或历史残留，不得擅自加入 reactor。
- 本项目不创建 `synapse-starter-*`，不创建 starter 聚合包，不创建 demo / example / sample application。

## 3. 修改前必读文档

所有任务必须先读：

- `README.md`
- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/01-module-boundary.md`
- `docs/phase-2/02-phase-2-roadmap.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/modules/README.md`

涉及 `synapse-cloud`、OpenFeign、服务间 Header 或远程错误解码时，必须额外读取：

- `docs/phase-2/04-cloud-context-propagation.md`
- `docs/modules/synapse-cloud.md`
- `skills/synapse-cloud/SKILL.md`

涉及 WebMVC / WebFlux 时，如果对应 Skill 存在，必须读取：

- `skills/synapse-webmvc/SKILL.md`
- `skills/synapse-webflux/SKILL.md`

涉及其他模块时，必须读取对应模块手册：

```text
docs/modules/<module-name>.md
```

如果对应 Skill 已存在，也必须读取：

```text
skills/<module-name>/SKILL.md
```

不存在的 Skill 不要臆造为已存在约束；可以在模块完成并测试通过后新增。

## 4. 架构硬约束

禁止在 Framework 中新增：

- `@SpringBootApplication` 生产启动类。
- `SpringApplication.run(...)` 生产启动入口。
- 业务 Controller。
- 业务 Application Service。
- 业务 Domain Model。
- 业务 Entity / Mapper / Repository / Service。
- 业务数据库 migration。
- 用户、角色、菜单、组织、部门、字典等平台业务模型。
- starter 聚合包。
- demo / example / sample application。
- Admin UI、业务页面或前端应用。
- Gateway / IAM / Message / File / Config / Task 等可启动平台服务。

允许出现：

- 技术模块的 AutoConfiguration。
- 技术模块的 Properties。
- 技术模块的 SPI / Port / Adapter。
- 技术型基础模型，例如 `BaseEntity`、`AuditableEntity`、`VersionedEntity`。
- `src/test` 下的测试 fixture、测试配置、测试 Controller 或测试 Application。

## 5. 模块边界速查

| 模块 | 定位 | 禁止滑向 |
| --- | --- | --- |
| `synapse-bom` | 依赖版本管理 | starter / 自动启用能力 |
| `synapse-core` | 最小核心契约 | Web / Security / Data / MQ 具体技术栈 |
| `synapse-webmvc` | Servlet MVC 技术支撑 | WebFlux / Gateway / 业务 Controller |
| `synapse-webflux` | WebFlux 技术支撑 | Gateway 服务 / 路由管理 / 网关鉴权业务 |
| `synapse-cloud` | OpenFeign 服务间调用技术适配 | Gateway / 注册中心 / 配置中心 / IAM |
| `synapse-time` | 时间和时区技术支撑 | 时区后台 / 用户资料管理 |
| `synapse-config` | 配置抽象、运行时读取和类型解析 | config-service / 配置中心后台 |
| `synapse-i18n` | 国际化消息解析抽象 | i18n-resource-center / 翻译后台 |
| `synapse-data` | 数据层技术支撑 | 业务 Entity / Mapper / Repository / Service |
| `synapse-cache` | 缓存、锁、限流、幂等基础设施 | 业务缓存规则 / 缓存管理后台 |
| `synapse-security` | Web 无关安全主体、权限检查和安全上下文 | IAM / 登录认证 / 用户角色菜单管理 / Servlet Filter |
| `synapse-security-webmvc` | trusted-header Servlet MVC 适配 | Spring Security FilterChain / OAuth2 / IAM |
| `synapse-oauth2-core` | JWT claim、token、validator、denylist 和 BearerTokenProvider 契约 | Web / Security / 签发私钥 / Resource Server |
| `synapse-oauth2-authorization-server-support` | JWT 签发、RSAKey、JWKSource、JwtEncoder 技术支持 | 登录 / RegisteredClient / Authorization Code / IAM |
| `synapse-oauth2-resource-server-webmvc` | Servlet OAuth2 Resource Server 技术适配 | 签发私钥 / Authorization Server / IAM |
| `synapse-oauth2-resource-server-webflux` | Reactive OAuth2 Resource Server 技术适配 | Gateway 服务 / 签发私钥 / IAM |
| `synapse-audit` | 审计事件契约 | 审计中心 / 查询 API / 报表 |
| `synapse-file` | 文件存储抽象 | 文件中心 / 附件表 / 文件权限业务 |
| `synapse-mq` | MQ 消息契约和发布消费 SPI | 消息中心 / 站内信 / 短信 / 邮件 / 模板 |

更详细规则以 `docs/phase-2/01-module-boundary.md` 和各模块手册为准。

## 6. 依赖与 POM 规则

- 版本基线以 `pom.xml` 和 `synapse-bom/pom.xml` 为准，不在本文件维护版本表。
- 新增三方依赖前必须说明必要性、替代方案、影响范围和是否会把 Platform 能力带入 Framework。
- 新增模块必须同时更新 root `pom.xml`、`synapse-bom/pom.xml`、`README.md`、`docs/modules/README.md`、模块手册和必要 Skill。
- 不得为了复用而让 WebMVC / WebFlux / Cloud / MQ / Security 产生反向依赖或循环依赖。
- `synapse-cloud` 不得依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`。
- 所有公开 `@ConfigurationProperties` 必须生成 Spring Boot Configuration Metadata，且配置字段必须有可用于 IDE 展示的说明。
- `spring-boot-configuration-processor` 只能作为编译期 annotation processor 使用，不得成为消费方运行时强依赖。
- `additional-spring-configuration-metadata.json` 只能补充自动生成不足的信息，不得重复维护全部普通属性。

## 7. 开发流程

每个任务开始前必须输出简短自查：

1. 本次目标。
2. 修改范围。
3. 明确不做内容。
4. 是否修改 POM 或新增依赖。
5. 是否新增 module。
6. 是否可能触碰启动类、Controller、业务持久化、starter、demo。
7. 需要执行的验证命令。

执行要求：

- 优先小步修改，不做无关重构。
- 发现边界冲突时停止并说明，不要强行实现。
- 不通过删除测试、降低断言或吞异常来制造通过。
- 不把规划能力写成当前已实现能力。
- 不自动提交，除非用户明确要求。

## 8. 测试与验收

代码变更至少执行相关模块测试。影响多个模块时执行全量验证。

常用命令：

```bash
mvn -q validate
mvn -q test
mvn -q clean test
git diff --check
```

边界检查命令：

```bash
rg -n "@SpringBootApplication|SpringApplication\.run" .
rg -n "@RestController\b|@Controller\b|@RequestMapping\b|@GetMapping\b|@PostMapping\b" '*/src/main/java'
rg -n "@TableName\b|BaseMapper\b|IService\b|ServiceImpl\b|CREATE TABLE|create table" '*/src/main'
rg -n "starter|demo|example|sample" README.md AGENTS.md docs pom.xml synapse-bom/pom.xml
rg -n "file-service|message-service|config-service|audit-service|task-service|iam-service|配置中心|文件中心|消息中心|审计中心|任务中心" README.md AGENTS.md docs
find . -path "*/target/classes/META-INF/spring-configuration-metadata.json" -print
```

命中不一定违规，但必须说明是否为：

- 禁止项说明。
- 历史说明。
- 检查命令。
- 测试 fixture。
- 真实生产代码问题。

## 9. 文档与 Skill 规则

模块发生能力变化时，需要同步更新：

- `README.md`
- `docs/modules/README.md`
- `docs/modules/<module-name>.md`
- `docs/phase-2/*` 中相关规划或状态
- 已存在的 `skills/<module-name>/SKILL.md`

公开配置项发生变化时，还必须同步检查：

- Properties 字段或 record component Javadoc。
- 生成的 `spring-configuration-metadata.json`。
- 必要的 additional metadata hints。
- 模块手册和 Skill 中的配置说明。

新增或重构完成一个技术模块并通过测试后，可以新增对应 Skill：

```text
skills/<module-name>/SKILL.md
```

Skill 是最佳实践，不是过程日志；不得写“计划中但未实现”的能力为当前事实。

## 10. 输出格式

任务完成后必须输出：

1. 新增文件列表。
2. 修改文件列表。
3. 删除文件列表。
4. 核心实现或文档变更摘要。
5. 执行过的验证命令。
6. 验证结果。
7. 合理命中说明。
8. 未完成事项。
9. 风险点。
10. 是否建议提交，以及推荐 commit message。

## 11. 默认原则

- 正确性优先于速度。
- 边界优先于功能堆叠。
- 技术底座优先于业务闭环。
- 当前事实优先于历史计划。
- 先读文档，再改代码。
- 先做最小技术闭环，再做扩展能力。
