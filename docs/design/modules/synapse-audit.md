# synapse-audit 设计说明

## 1. 边界决策

Audit 负责审计语义，Messaging 负责传输与可靠登记。依赖方向固定为：

```text
synapse-audit -> synapse-messaging
```

AuditPublisher 只能委托 BestEffortMessagePublisher 或 ReliableMessagePublisher，不访问 StreamBridge、
Broker、OutboxStore 或 Retry Scheduler。

## 2. 处理链路

```text
explicit AuditEvent / @Audited
  -> AuditEventContextEnricher
  -> AuditSanitizer
  -> AuditFailurePolicy
  -> BestEffortMessagePublisher | ReliableMessagePublisher
```

上下文补齐生成 eventId、subject/principal、tenant、traceId、source service、occurredAt 和 outcome。
显式字段优先，不伪造 system/unknown 主体。

## 3. 失败策略

- CONTINUE：普通审计失败只告警，不改变业务结果。
- ROLLBACK：关键审计必须在活动本地事务内登记 Outbox，失败向外传播。

业务异常发生后，切面仍尝试记录 FAILURE；审计异常作为 suppressed error 附加，不覆盖原业务异常。

## 4. 数据安全

默认 Sanitizer 屏蔽 credential 类 key。注解切面不采集参数、返回值和完整请求体，避免敏感信息
在进入脱敏器前已被复制。调用方仍应只放置必要的低敏技术属性。

## 5. 兼容性

旧 AuditRecorder/AuditLogPort 保留为本地显式输出兼容入口，不参与默认消息链路。
新增集成统一面向 AuditPublisher。

## 6. 禁止项

- 审计表、Repository、查询 API、报表和后台。
- 第二套消息 Envelope、Transport、Outbox 或消费者运行时。
- Broker SDK、Binder、数据库和缓存实现。
- Controller、migration、starter、demo 或可启动 audit-service。
