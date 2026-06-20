# 事务规范

## 1. 适用范围

本规范适用于依赖 Synapse Framework 的 Platform 服务和业务应用。Framework 只提供技术契约，
不创建 `synapse-transaction` 模块，也不定义 `@SynapseTransactional` 等自定义事务注解。

## 2. 事务边界

- 事务边界放在应用服务层的完整用例方法上，不放在 Controller、Entity、Mapper 或纯查询组装器上。
- 使用 Spring `@Transactional`，让事务语义对团队和工具保持可见。
- 一个本地事务只绑定一个已确定的数据源；数据源选择必须发生在事务开始前。
- 活动事务中禁止切换数据源。需要访问其他数据源时拆分用例或采用异步最终一致性。

## 3. 回滚规则

- `RuntimeException` 和 `Error` 使用 Spring 默认回滚规则。
- Checked Exception 默认不回滚。只有异常确实表示当前事务不可提交时，才显式配置 `rollbackFor`。
- 不得为了测试通过而吞异常、将失败转换为成功，或在事务外补写关键数据。
- `readOnly=true` 是连接池、ORM 和数据库可能采用的优化提示，不是权限或安全控制，也不保证物理只读。

## 4. Spring 代理限制

- 同一对象内部自调用通常不会经过 Spring 事务代理，目标方法上的 `@Transactional` 不生效。
- private、final 或非代理可见的方法不应承担事务边界。
- 需要复用事务用例时，将边界移动到独立 Bean 的 public 方法，或重构调用关系；不要手工获取自身代理掩盖设计问题。

## 5. 传播行为

- 默认使用 `Propagation.REQUIRED`，让同一业务用例共享事务。
- `REQUIRES_NEW` 会挂起外层事务并占用额外连接，只允许用于确实需要独立提交且已评估连接池容量的短操作。
- 不得用 `REQUIRES_NEW` 规避聚合一致性、隐藏失败或制造“主事务回滚但关键副作用已提交”的状态。
- 远程 HTTP/RPC、Broker 等待和长时间计算不得长期占用数据库事务；先完成本地状态变更，再通过 Outbox 异步协作。

## 6. Messaging 与 Audit

- `ReliableMessagePublisher` 只在活动本地事务中登记发送方本地 Outbox，不同步等待 Broker。
- `OutboxStore` 必须与业务数据使用同一数据源并参与当前事务；独立连接或独立事务会破坏原子性。
- 关键审计使用 `AuditFailurePolicy.ROLLBACK`，必须与业务操作共享当前本地事务和可靠 Outbox。
- 普通审计可使用 `CONTINUE`，其失败不得伪装为已可靠记录。
- Outbox 投递采用 At-least-once；消费者按 `eventId`，缺失时按 `messageId` 幂等。

## 7. 分布式一致性选择

按以下顺序选择方案：

1. 单服务本地事务。
2. 本地事务 + Outbox + 消费幂等。
3. 业务可补偿时使用 Saga；资源需要显式 Try/Confirm/Cancel 时才考虑 TCC。
4. Seata 只由具体 Platform/Application 按需接入，Framework 不默认依赖，也不承诺跨库事务。

优先选择可观察、可重放、可补偿的最终一致性，不把远程调用包装成看似原子的长事务。

## 8. 评审清单

- 事务是否覆盖一个完整且有限的应用用例？
- 数据源是否在事务开始前确定？
- 是否在事务中等待远程系统或执行长任务？
- Checked Exception 的回滚配置是否有业务理由？
- `REQUIRES_NEW` 是否可能耗尽连接或提前提交副作用？
- Reliable Messaging 和关键 Audit 是否与业务数据共享事务？
- 消费端是否具备持久化幂等实现？
