# Spring Boot Admin 对标分析

## 1. 项目定位

Spring Boot Admin 是用于管理和监控暴露 Actuator endpoints 的 Spring Boot Web 应用的 Admin UI。

来源：https://github.com/codecentric/spring-boot-admin

## 2. 值得借鉴

Spring Boot Admin 不是业务后台框架，但它补足了后台框架生产化必需的“运维管理台”能力：

- 应用实例列表
- 健康状态
- Actuator endpoint 展示
- 日志查看
- 指标查看
- 环境信息
- 线程/内存等诊断入口

## 3. 对 Synapse 的决策

Synapse v0.1 不内置完整监控平台，但必须预留：

- Actuator
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- build info
- git info
- application version
- trace id
- request log

后续 `synapse-monitor` 可以集成 Spring Boot Admin 或其他监控栈。

## 4. 不建议照搬

- 不要把 Spring Boot Admin 当成系统管理后台。
- 不要让业务权限和 Actuator 权限混在一起。
- 生产环境 Actuator 端点必须最小暴露。

## 5. Codex 使用建议

涉及监控时，Codex 应只做：

- Actuator 配置
- 安全暴露策略
- 健康检查配置
- 版本信息输出
- 运维文档
