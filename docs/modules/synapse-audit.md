# synapse-audit 使用手册

## 1. 模块定位

`synapse-audit` 是 Synapse Framework 的审计事件契约模块。

一阶段它只定义审计事件结构、审计上下文、审计记录入口和审计输出端口，不实现审计落库、审计查询、审计后台或业务审计规则。

当前核心能力：

- `AuditEvent` 审计事件模型。
- `AuditSubject` 审计主体。
- `AuditTarget` 审计目标。
- `AuditOutcome` 审计结果。
- `AuditContext` 显式审计上下文。
- `AuditEventContextEnricher` 上下文补齐。
- `AuditRecorder` 审计记录入口。
- `AuditLogPort` 审计输出端口。
- `CompositeAuditLogPort` 多端口广播。
- `NoopAuditLogPort` 默认空实现。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-audit`：

- 需要统一审计事件结构。
- 需要记录技术操作或业务操作的审计事件。
- 需要从 `OperationContext` 自动补齐操作主体、traceId、requestId、source 等上下文。
- 需要通过 `AuditLogPort` 自定义审计输出，例如写数据库、日志系统、消息队列或外部审计平台。
- 需要在任务、补偿、异步流程中显式指定审计主体。

## 3. 不适用场景

`synapse-audit` 不适合承担以下职责：

- 审计表结构。
- 审计 Repository。
- 审计查询接口。
- 审计管理后台。
- 审计报表。
- 审计消息可靠投递。
- 审计归档策略。
- 审计保留周期。
- 业务操作枚举全集。
- 业务审计规则。

这些应由业务系统或平台服务实现。

## 4. Maven 引入

推荐先引入 BOM：

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

再引入 audit 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-audit</artifactId>
</dependency>
```

## 5. 核心能力

### 5.1 审计事件

核心类型：

```java
AuditEvent
AuditSubject
AuditTarget
AuditOutcome
```

`AuditEvent` 字段：

```text
action
subject
target
occurredAt
outcome
traceId
message
attributes
```

`subject` 和 `traceId` 可以先不显式设置，由上下文补齐。

### 5.2 上下文补齐

核心类型：

```java
AuditEventContextEnricher
```

补齐优先级：

```text
显式事件字段
  -> AuditContext
  -> OperationContextProvider
```

补齐内容：

- `subject`
- `traceId`
- `operation.actor.*`
- `operation.initiator.*`
- `operation.requestId`
- `operation.source.*`

说明：

- 不伪造默认主体。
- 不覆盖调用方已经写入的 attributes。
- `SYSTEM` 和 `UNKNOWN` 类型 actor 不会被自动补为审计主体。

### 5.3 AuditContext

核心类型：

```java
AuditContext
AuditContextSnapshot
AuditContextScope
```

适用场景：

- 定时任务。
- 补偿流程。
- 批处理。
- 内部调用。
- 无法从 `OperationContext` 推导审计主体的场景。

推荐用法：

```java
AuditSubject subject = new AuditSubject("JOB", "daily-sync", null);
AuditContextSnapshot snapshot = new AuditContextSnapshot(subject, "trace-001");

try (AuditContextScope ignored = AuditContext.scope(snapshot)) {
    auditRecorder.record(event);
}
```

### 5.4 审计记录入口

核心类型：

```java
AuditRecorder
```

处理流程：

```text
AuditRecorder.record(event)
  -> AuditEventContextEnricher.enrich(event)
  -> AuditEvent.requireRecordable()
  -> AuditLogPort.record(enrichedEvent)
```

记录前要求最终事件具备：

- `subject`
- `traceId`

否则会抛出异常，避免审计事件不可追溯。

### 5.5 审计输出端口

核心类型：

```java
AuditLogPort
NoopAuditLogPort
CompositeAuditLogPort
```

说明：

- `AuditLogPort` 是审计输出端口。
- `NoopAuditLogPort` 默认不输出，只做参数校验。
- 存在多个 `AuditLogPort` 时，会通过 `CompositeAuditLogPort` 依次调用。

## 6. 快速使用

### 6.1 记录审计事件

```java
AuditEvent event = AuditEvent.builder()
        .action("resource.create")
        .target(new AuditTarget("RESOURCE", "10001"))
        .occurredAt(Instant.now())
        .outcome(AuditOutcome.SUCCESS)
        .message("创建资源")
        .build();

auditRecorder.record(event);
```

如果当前存在 `OperationContext`，`subject` 和 `traceId` 可以由上下文补齐。

### 6.2 显式指定 subject

```java
AuditEvent event = AuditEvent.builder()
        .action("resource.delete")
        .subject(new AuditSubject("USER", "10001", null))
        .target(new AuditTarget("RESOURCE", "20001"))
        .occurredAt(Instant.now())
        .outcome(AuditOutcome.SUCCESS)
        .traceId("trace-001")
        .build();

auditRecorder.record(event);
```

### 6.3 实现审计输出端口

```java
@Bean
AuditLogPort auditLogPort() {
    return event -> {
        // 写入业务系统自己的审计表、日志系统或消息队列
    };
}
```

注意：审计表、Repository、消息发送逻辑都属于消费方，不属于 `synapse-audit`。

## 7. 扩展方式

### 7.1 自定义 AuditLogPort

```java
@Bean
AuditLogPort databaseAuditLogPort() {
    return new DatabaseAuditLogPort(...);
}
```

### 7.2 多个 AuditLogPort

可以同时注册多个端口，例如：

```java
@Bean
AuditLogPort databaseAuditLogPort() { ... }

@Bean
AuditLogPort messageAuditLogPort() { ... }
```

默认 `AuditRecorder` 会组合后依次调用。

### 7.3 自定义 OperationContextProvider

```java
@Bean
OperationContextProvider operationContextProvider() {
    return new CustomOperationContextProvider();
}
```

`AuditEventContextEnricher` 会优先使用容器中的实现。

## 8. 配置项

`synapse-audit` 一阶段没有独立配置项。

行为主要通过 Bean 扩展：

- `AuditLogPort`
- `AuditEventContextEnricher`
- `AuditRecorder`
- `OperationContextProvider`

## 9. 边界与注意事项

### 9.1 audit 不依赖 data

`synapse-audit` 不负责落库，因此不依赖 `synapse-data`。

正确方向是：

```text
业务系统 / 平台服务
  -> 实现 AuditLogPort
  -> 自行选择 DB / MQ / Log / 外部审计平台
```

### 9.2 不默认使用 system

审计事件如果无法补齐 subject 或 traceId，会在记录前失败。

这样可以避免异步、MQ、任务场景默认写成无法追溯的主体。

### 9.3 attributes 不是业务数据仓库

`attributes` 只用于补充技术排查信息。不要把大段业务对象或敏感明文放入 attributes。

### 9.4 CompositeAuditLogPort 不做失败隔离

多个端口依次调用时，如果某个端口抛异常，后续端口可能不会执行。

如果需要异步、重试、隔离、可靠投递，应在消费方端口实现中处理。

## 10. 常见问题

### Q1：为什么 audit 不直接落库？

因为审计表结构、索引、保留周期、查询维度通常和业务系统或平台服务强相关。framework 只提供契约和端口。

### Q2：为什么没有 subject 时不自动写 system？

默认写 system 会让真实操作者丢失，尤其是异步、MQ、任务和补偿场景。缺少上下文时应显式建立 AuditContext 或 OperationContext。

### Q3：AuditContext 和 OperationContext 有什么区别？

`OperationContext` 是 core 的通用操作上下文，适合跨 data、audit、mq、security 共享。

`AuditContext` 是 audit 模块的显式补充入口，适合只想为审计指定 subject 和 traceId 的特殊场景。

### Q4：可以把审计事件发到 MQ 吗？

可以，但应由业务系统或平台服务实现 `AuditLogPort`，在端口实现中发送 MQ。`synapse-audit` 不直接依赖 MQ。

### Q5：attributes 会自动处理敏感属性吗？

模块会做基础处理，但这不是完整数据安全方案。调用方仍应避免写入敏感明文。
