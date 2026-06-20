# 自动配置契约测试标准

## 1. 目标

自动配置测试验证“应用在不同 classpath、配置和用户扩展下能否正确启动”，不是简单统计 Bean 数量。
测试保留在能力所属模块，不创建聚合测试模块。

## 2. Runner 选择

- 非 Web 自动配置使用 `ApplicationContextRunner`。
- Servlet 自动配置使用 `WebApplicationContextRunner`。
- Reactive 自动配置使用 `ReactiveWebApplicationContextRunner`。
- 可选依赖缺失使用 `FilteredClassLoader`，不得通过删除生产条件模拟。

## 3. 契约矩阵

每个自动配置按适用性覆盖以下场景；不适用项应在测试或模块文档中有明确理由：

1. 条件满足时正常装配核心 Bean。
2. 可选 Bean 缺失时正常启动，不创建依赖该 Bean 的能力。
3. 可选 Class 缺失时正常启动。
4. 用户同类型 Bean 覆盖默认实现。
5. `enabled=false` 不创建该能力的 Bean。
6. 显式启用但必要依赖缺失时快速失败，并断言可诊断的错误原因。
7. 多 Bean 行为明确：组合、选择唯一 Bean 或快速失败。
8. 局部扩展只替换目标能力，不关闭无关默认 Bean。
9. Servlet 与 Reactive 自动配置互不进入错误的应用类型。
10. Configuration Properties 覆盖默认值、绑定、校验和非法值。
11. `AutoConfiguration.imports` 中包含正式自动配置且无重复。
12. 公开配置生成 `spring-configuration-metadata.json`，关键属性包含说明和默认值。

## 4. 测试原则

- 优先断言公开契约类型，不依赖私有 Bean 名。
- 快速失败测试必须断言根因或稳定消息，不只断言 `context.hasFailed()`。
- 用户覆盖测试传入真实接口实现，避免只反射检查注解。
- 缺少 Class 测试过滤最小包或类型，避免误删无关依赖。
- 不启动真实 Broker、数据库或网络服务；集成测试确有必要时使用容器并明确范围。
- 不为增加数量复制同一断言；一个测试可覆盖一组强相关条件。

## 5. 当前仓库执行方式

```bash
mvn -pl <module> -am test
mvn clean verify
```

新增或修改 `@ConfigurationProperties` 时，同时检查字段 Javadoc、绑定测试和生成 metadata。
新增 AutoConfiguration 时，同时更新 `AutoConfiguration.imports`；Phase 12 架构脚本负责全仓库存在性和重复检查。

## 6. Review 清单

- 条件注解检查的类型与 Bean 方法实际注入类型是否一致？
- 可选依赖缺失是否仍能创建 ApplicationContext？
- 用户 Bean 是否真正优先，而不是同时产生歧义？
- 显式开启的必要依赖缺失是否快速失败？
- MVC/WebFlux 是否使用正确 Runner 验证隔离？
- 配置名称、默认值、Javadoc、metadata 和模块文档是否一致？
