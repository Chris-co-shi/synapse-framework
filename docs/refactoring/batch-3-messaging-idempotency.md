# 批次 3：Messaging 原子幂等

## 状态

实现完成，`PR Verify` 已通过。目标分支：`refactor/batch-3-messaging-idempotency`。

## 冻结语义

- 删除 `isProcessed/markProcessed` 两步式检查。
- Store 使用 `claim/complete/release` 原子生命周期。
- 处理状态语义为 `PROCESSING`、`COMPLETED`、`RETRYABLE`。
- claim 必须带有限租约；complete/release 必须校验 claimId。
- 幂等键由 `consumerId + handlerId + messageType + eventId/messageId` 组成。
- `handlerId` 必须稳定，不得使用 Java 类名。
- 正在由其他消费者处理时返回 `RETRY`，不能当作完成并提前 ACK。
- Framework 不承诺 Exactly-once；关键业务仍需业务幂等或本地事务消费记录。

## 验证

- GitHub Actions `PR Verify` 全部步骤通过。
- 覆盖原子状态生命周期、处理中消息、异常释放、消费方/Handler 作用域和自动配置快速失败。
- Qodana 工作流未产生 PR 评论或扫描产物，其失败单独作为扫描工作流配置问题处理，不作为本批次代码失败结论。

## 边界

本批次不提供 JDBC、Redis 等生产存储实现，不修改 Outbox、Audit 事务和消息身份信任协议。
