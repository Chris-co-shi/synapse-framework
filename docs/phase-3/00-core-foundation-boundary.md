# Phase 3 Core Foundation Boundary

本文档固化 Synapse Framework 第三阶段 Core Foundation 的职责边界。第三阶段不是新增平台服务，而是在现有 framework modules 上完成契约一致性、依赖收敛、上下文闭环和可验证性强化。

## 1. 阶段目标

第三阶段目标：

> 在不引入业务语义、不创建可启动服务的前提下，强化 core、web、security、data、cache、audit 的公共契约和跨模块协作链路，使其可以稳定支撑 Synapse Platform 与业务系统。

第三阶段重点处理：

- WebMVC / WebFlux 响应、异常和上下文契约一致性。
- SecurityContext 与 OperationContext 的适配和生命周期边界。
- Data 审计填充与依赖边界。
- Cache 锁、限流、幂等和缓存契约的失败语义。
- Audit 上下文补齐、输出端口和失败策略。
- 模块测试、自动配置测试和边界验证。

## 2. 本阶段目标模块

```text
synapse-core
synapse-webmvc
synapse-webflux
synapse-security
synapse-data
synapse-cache
synapse-audit
```

其他已实现模块不在本阶段主动扩展。只有当跨模块契约变更影响 `synapse-cloud`、`synapse-mq`、`synapse-time`、`synapse-config`、`synapse-i18n`、`synapse-oauth2` 或 `synapse-file` 时，才允许做最小兼容修改。

## 3. 允许内容

第三阶段允许在 Framework 中提供：

- 纯技术契约、SPI、Port 和 Adapter。
- 不包含业务语义的通用模型。
- Spring Boot AutoConfiguration。
- 消费方可覆盖的默认轻量实现。
- HTTP、Reactor、ThreadLocal、MQ carrier 之间的上下文适配。
- 统一响应、异常映射、trace 和 request context。
- 数据访问层通用插件与审计字段填充。
- Redis 缓存、锁、限流和幂等技术原语。
- 审计事件模型、上下文补齐和输出端口。
- 单元测试、自动配置测试、集成测试和边界测试。
- 模块手册与 Skill 最佳实践。

## 4. 禁止内容

第三阶段禁止在 Framework 中新增：

- `@SpringBootApplication` 生产启动类。
- Gateway、IAM、Audit Center、Cache Center 等可启动平台服务。
- 业务 Controller、Application Service、Domain Model。
- 业务 Entity、Mapper、Repository、Service。
- 业务数据库 migration。
- 用户、角色、菜单、组织、订单、库存等业务模型。
- 业务权限码、DataScope、ABAC 规则。
- 业务缓存 key、业务锁粒度、业务限流策略和业务幂等语义。
- 审计查询 API、审计表结构、审计报表和审计后台。
- starter 聚合包。
- demo、example、sample application。

## 5. 依赖方向

固定依赖方向：

```text
synapse-core
  <- synapse-webmvc
  <- synapse-webflux
  <- synapse-security
  <- synapse-data
  <- synapse-cache
  <- synapse-audit
```

允许上层模块依赖 `synapse-core`，禁止 `synapse-core` 反向依赖 Web、Security、Data、Cache、Audit 或具体基础设施。

额外约束：

- `synapse-data` 不得依赖 `synapse-security`。
- `synapse-audit` 不得依赖 `synapse-data`、`synapse-cache` 或 `synapse-mq`。
- `synapse-webmvc` 与 `synapse-webflux` 不得互相依赖。
- `synapse-webflux` 不得引入 `spring-webmvc` 或 `jakarta.servlet`。
- `synapse-security` 不得实现 IAM、OAuth2 Authorization Server 或业务用户中心。

## 6. 上下文边界

### 6.1 OperationContext

`OperationContext` 是跨模块技术上下文，负责承载：

- actor。
- initiator。
- source。
- traceId。
- requestId。
- tenantId 承载位。
- occurredAt。
- attributes。

它不是用户领域模型，也不是权限模型。

### 6.2 SecurityContext

`SecurityContext` 只表达当前已认证主体和权限快照。Security 可以单向适配到 OperationContext，但 core、data、audit 不得反向读取 security。

### 6.3 无用户场景

Job、MQ、Async、补偿任务等场景必须显式建立可追溯 actor 和 source。Framework 禁止在缺少上下文时静默使用固定字符串 `system`。

## 7. 完成判定

第三阶段完成不以“新增类数量”为标准，而以以下条件为准：

- 公共契约在 WebMVC、WebFlux、Security、Data、Cache、Audit 之间语义一致。
- 模块依赖与文档定位一致。
- 上下文进入、传播、恢复、清理和审计链路可验证。
- 消费方 Bean 覆盖行为稳定。
- 不存在通过吞异常、降低断言或默认伪造主体获得的伪闭环。
- 根工程构建、测试和边界检查通过。
