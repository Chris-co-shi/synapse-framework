# 可靠消息 Outbox / Retry / DLQ / Compensation 引用文档

`synapse-message` 的可靠消息层用于把异常保障从具体 MQ 中抽离出来。Redis Pub/Sub、RocketMQ 或其他 MQ 只实现 `MessageTransport`，重试、死信、补偿和多集群抢占由框架状态库处理。

## 1. 启用配置

```yaml
synapse:
  message:
    reliable:
      enabled: true
      scheduler:
        enabled: true
        batch-size: 100
        lock-ttl: 30s
        interval: 5s
      retry:
        max-attempts: 5
        initial-interval: 10s
        multiplier: 2.0
        max-interval: 10m
```

可靠消息默认关闭。启用后还需要存在 `DataSource`、MyBatis-Plus 和 `SqlSessionFactory`。

## 2. 事务边界

消费方应在业务本地事务内调用：

```java
outboxAppender.append(messageEnvelope, idempotencyKey);
```

业务数据和 outbox 记录同库同事务提交，避免业务提交后异步消息丢失。框架不包业务事务，不替消费方决定事务传播策略。

## 3. 技术表 DDL 参考

消费方可用 Flyway/Liquibase 执行以下结构，框架不自动创建业务库对象。

```sql
create table synapse_message_outbox (
    message_id varchar(64) primary key,
    topic varchar(255) not null,
    tag varchar(128),
    message_key varchar(255),
    headers_json text,
    payload text,
    trace_id varchar(128),
    tenant_id varchar(128),
    message_created_at timestamp not null,
    status varchar(32) not null,
    attempt int not null,
    next_retry_at timestamp not null,
    locked_by varchar(128),
    locked_until timestamp,
    last_error text,
    idempotency_key varchar(255),
    created_at timestamp not null,
    updated_at timestamp not null,
    version int not null
);

create index idx_synapse_msg_outbox_due on synapse_message_outbox(status, next_retry_at);
create index idx_synapse_msg_outbox_lock on synapse_message_outbox(locked_until);
create unique index uk_synapse_msg_outbox_idem on synapse_message_outbox(idempotency_key);

create table synapse_message_dead_letter (
    message_id varchar(64) primary key,
    topic varchar(255) not null,
    reason text,
    created_at timestamp not null
);

create table synapse_message_compensation (
    compensation_id varchar(64) primary key,
    message_id varchar(64) not null,
    handler_name varchar(255) not null,
    payload text,
    status varchar(32) not null,
    error_message text,
    created_at timestamp not null,
    updated_at timestamp not null
);
```

如业务幂等 key 可为空，部分数据库需要使用过滤唯一索引替代普通唯一索引。

## 4. 多集群并发

- Dispatcher 扫描 `PENDING` 和 `RETRY` 且 `next_retry_at <= now` 的消息。
- claim 时写入 `locked_by`、`locked_until` 并递增 `version`。
- 同一消息只有一个 worker 能成功更新租约。
- worker 宕机后，超过 `locked_until` 的消息可被其他节点重新 claim。

## 5. 可靠性边界

- 发送成功但状态更新失败时可能重复投递，消费端必须按 `messageId` 或 `idempotencyKey` 幂等。
- 超过最大重试次数进入 DLQ。
- 补偿逻辑由消费方实现 `CompensationHandler`，框架只记录和调度技术状态。
- 不提供跨系统强一致事务，目标是最终一致、可恢复、可观测、可补偿。
