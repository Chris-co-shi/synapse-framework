# synapse-cache Skill

## 职责

`synapse-cache` 只提供缓存、分布式锁、限流、幂等等通用技术能力。

## 禁止事项

- 不做缓存管理后台。
- 不沉淀业务缓存规则。
- 不新增业务 Controller、Entity、Mapper、Repository、Service。
- 不创建 starter、demo、example、sample application。

## 标准实现

- key 生成必须可预测、可测试。
- 锁、限流、幂等必须有失败语义和边界测试。
- Redis 适配不得引入业务语义。
- Redis 默认 Bean 必须以 `@ConditionalOnBean(StringRedisTemplate.class)` 为前置条件，缺少 Redis Bean 时正常退让。

## 验证

- 运行 `mvn -q -pl synapse-cache -am test`。
- 检查无业务模型和启动类。
- 修改 `SynapseCacheProperties` 时必须验证 `META-INF/spring-configuration-metadata.json` 包含 `synapse.cache.*` 属性、类型和说明。
