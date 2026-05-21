---
name: synapse-common
description: Synapse Common 基础能力最佳实践。Use when Codex implements or reviews synapse-common code involving error codes, business exceptions, common enums, IDs, time utilities, masking utilities, constants, or shared Java foundation types.
---

# Synapse Common

## 必读

- `AGENTS.md`
- `docs/00-positioning.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/08-ai-development-rules.md`
- `docs/10-technical-foundation-baseline.md`

## 职责和边界

- 只放跨模块可复用的 Java 基础能力。
- 可以包含错误码、业务异常、通用枚举、ID 接口/实现、时间工具、脱敏工具、通用常量。
- 不依赖 Spring Web、Spring Security、MyBatis-Plus、Redis、Admin 或业务模块。
- 不放业务常量和业务规则。

## 推荐包结构

```text
com.indigo.synapse.common
├── error
├── exception
├── id
├── time
├── mask
└── constant
```

## 标准实现模式

- 错误码使用稳定字符串，不因文案调整改变 code。
- 业务异常必须持有错误码，不返回 `null` 表示错误。
- 工具类必须无状态、线程安全、可单元测试。
- ID 能力优先提供接口，再给出纯 Java 默认实现。
- 时间工具优先接收 `Clock` 和 `ZoneId`，避免隐式时区带来测试不稳定。
- 脱敏工具必须默认保守，格式不合法时明确抛出异常。

## 测试要求

- 覆盖正常流程、空参数、非法参数。
- 修改错误码或异常时必须补充兼容性测试。
- 模块完成后运行模块测试，关键变更运行根目录 `mvn clean test`。

## 常见错误

- 在 common 引入 Spring 或持久化依赖。
- 把业务模块常量放入 common。
- 修改已有错误码字符串导致前端或调用方不兼容。

## 示例任务拆分

- 补充通用错误码。
- 实现业务异常构造行为。
- 增加脱敏工具及单元测试。
