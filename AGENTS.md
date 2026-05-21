# Synapse Framework AI 协作总规范

本文件是 Synapse Framework 仓库的 AI Agent 总入口。所有 Codex、代码 Agent、Review Agent、测试 Agent 在修改本仓库前必须读取本文件。

## 1. 项目定位

Synapse Framework 是面向企业内部应用的 Java 通用技术基座和后台快速开发底座。

核心目标：

1. 提供 Web、Data、Cache、Security、Audit、Starter 等可复用技术基座能力。
2. 提供统一认证、授权、菜单、组织、数据权限、审计日志、字典配置等后台验证能力。
3. 提供标准化 Java 后端开发包结构、接口规范、数据库规范和测试规范。
4. 提供可被 AI Agent 长期协作的工程约束，避免不同 Agent 生成风格冲突的代码。
5. 支持未来扩展到多租户、工作流、消息通知、文件存储、代码生成器、业务模块脚手架。

## 2. 当前阶段

当前处于框架 v0.1 阶段：

- 优先单体模块化，不直接拆微服务。
- 优先完成通用技术基座，不追求完整后台管理系统或低代码平台。
- 优先保证工程边界、测试闭环和可维护性。

## 3. 技术基线

- Java 21
- Spring Boot 3.5.14
- Spring Security 6.5.x
- OAuth2 Authorization Server + Resource Server
- JWT + JWK
- MyBatis-Plus 3.5.9，完整使用官方能力
- dynamic-datasource Spring Boot 3 starter，配置级多数据源切换
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

## 4. 强制读取文档

任何后端开发任务必须先读取：

- `docs/00-positioning.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/03-package-rules.md`
- `docs/04-database-rules.md`
- `docs/05-api-rules.md`
- `docs/06-security-rules.md`
- `docs/07-test-rules.md`
- `docs/08-ai-development-rules.md`

涉及 MyBatis-Plus 或动态数据源必须读取：

- `skills/synapse-data/SKILL.md`

涉及 Redis、缓存、分布式锁或限流必须读取：

- `skills/synapse-cache/SKILL.md`

涉及 OAuth2、JWT、资源服务器或权限认证必须读取：

- `skills/synapse-security/SKILL.md`

涉及前端后台页面必须读取：

- `skills/synapse-vue-admin/SKILL.md`

涉及测试必须读取：

- `skills/synapse-test-engineering/SKILL.md`

## 5. 架构硬约束

### 5.1 禁止 Controller 直接访问持久化层

禁止：

```java
@RestController
class UserController {
    private final UserMapper userMapper;
}
```

必须：

```text
Controller -> Application Service -> Domain/Repository Port -> Repository Adapter -> Mapper
```

### 5.2 禁止 Domain Model 依赖 MyBatis-Plus

MyBatis-Plus 允许按官方最佳实践完整使用，包括 `IService`、`ServiceImpl` 和 ActiveRecord。

边界要求：

- Entity、Mapper、ServiceImpl、ActiveRecord 模型属于持久化实现，不允许直接暴露给 Controller 或前端。
- Domain Model 如独立存在，不承载 MyBatis-Plus 注解和持久化行为。
- 业务 API 返回对象必须是 response/result DTO，不直接返回 Entity。

### 5.3 Entity 只允许存在于 infrastructure.persistence.entity

MyBatis-Plus Entity 是持久化模型，不是领域模型，不允许直接暴露给 Controller 或前端。

### 5.4 Repository Port 与 Adapter 必须分离

- Port：`domain.repository`
- Adapter：`infrastructure.persistence.repository`
- Mapper：`infrastructure.persistence.mapper`
- Entity：`infrastructure.persistence.entity`

### 5.5 禁止随意引入依赖

新增三方依赖必须说明：

1. 解决什么问题？
2. 为什么不用已有依赖？
3. 是否有许可证风险？
4. 是否会影响启动速度、包体积、安全性？
5. 是否需要封装在 starter 或 adapter 里？

## 6. 代码修改前必须输出自查

每次实现前，Agent 必须先输出：

1. 本次涉及哪些模块？
2. 本次涉及哪些表？
3. 是否新增数据库 migration？
4. Entity 放在哪个包？
5. Mapper 放在哪个包？
6. Repository Port 放在哪个包？
7. Repository Adapter 放在哪个包？
8. 是否会使用 JdbcTemplate？
9. 是否会使用 JdbcClient？
10. 是否会使用 java.sql？
11. 是否会使用 IService / ServiceImpl？
12. 是否会使用 ActiveRecord Model<T>，边界如何控制？
13. 是否会让 MyBatis-Plus 模型直接暴露到 Controller 或前端？
14. 是否会让 Controller 直接依赖 Mapper？
15. 需要补充哪些测试？

## 6.1 模块完成后的 Skill 交付规则

每完成一个模块并通过测试后，必须为该模块沉淀 `skills/<module-name>/SKILL.md`。

要求：

- `SKILL.md` 是最佳实践，不是过程日志。
- 模块测试未通过，不允许把实现沉淀为最终 Skill。
- `SKILL.md` 必须覆盖模块职责和边界、推荐包结构、允许技术和禁止事项、标准实现模式、测试要求、常见错误、执行前必读文档、示例任务拆分方式。
- 后续同类模块开发前，Agent 必须先读取对应 `SKILL.md`。
- 如果实现过程中发现原 Skill 规则不适用，必须先说明原因，再更新 Skill。

## 7. 测试要求

每个模块至少覆盖：

- 正常流程
- 参数为空
- 参数非法
- 权限不足
- 数据不存在
- 重复提交
- 并发更新冲突
- 多租户隔离预留
- 数据权限越权预留
- 审计日志写入

## 8. 输出要求

任务完成后必须输出：

1. 修改文件列表
2. 新增文件列表
3. 删除文件列表
4. 测试命令
5. 测试结果
6. 设计取舍
7. 风险点
8. 后续建议

## 9. 禁止行为

- 禁止大范围重构无关代码。
- 禁止为了测试通过降低测试标准。
- 禁止吞异常。
- 禁止返回 `null` 表示错误。
- 禁止绕过权限校验。
- 禁止直接复制开源框架源码。
- 禁止把业务模块代码放进框架基础模块。
- 禁止把临时实验代码提交到主模块。
- 禁止使用宽泛的 `catch (Exception e)` 后只打印日志不处理。

## 10. 默认开发原则

- 正确性优先于速度。
- 可维护性优先于炫技。
- 框架边界优先于功能堆叠。
- 先有规则，再写代码。
- 先做最小闭环，再做扩展能力。
