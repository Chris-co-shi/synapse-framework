# 批次 4：Audit 事务语义

## 成功审计

- `BEST_EFFORT`：立即尝试投递；失败记录告警，不改变成功业务结果。
- `TRANSACTIONAL_OUTBOX`：在当前业务本地事务内调用 `ReliableMessagePublisher`，业务数据与 Audit Outbox 同时提交或回滚。

## 失败审计

- `NONE`：不记录失败审计，默认策略。
- `BEST_EFFORT_AFTER_ROLLBACK`：业务事务回滚完成后尝试投递。
- `REQUIRES_NEW_AFTER_ROLLBACK`：业务事务回滚完成后，在独立事务内登记 Outbox。
- `EXTERNAL_SINK`：业务事务回滚完成后调用应用提供的 `AuditFailureSink`。

失败审计异常只能作为 suppressed error 附加到原始业务异常，不得覆盖原异常。

## AOP 顺序

调用顺序固定为：

`Security(-200) -> Transaction(0) -> Audit(200) -> business method`

Framework 通过 `BeanFactoryPostProcessor` 校正 Spring 标准 Transaction Advisor 的 order，不创建新的 `AutoProxyCreator`。可靠 Publisher 存在但事务 Advisor 缺失或顺序异常时，启动快速失败。

## 边界

Framework 不提供 Outbox Store、外部失败 Sink 或事务管理器实现，也不承诺 Exactly-once。
