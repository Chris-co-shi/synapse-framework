# 12-External集成协议设计

## 1. 定位

External 集成协议用于规范框架与外部系统、外部中间件、外部服务的技术接入方式。

## 2. 集成方式

- HTTP / OpenFeign。
- WebClient。
- MQ。
- File Storage。
- Task Scheduler。
- External Client Adapter。

## 3. 标准契约

外部调用必须明确：

- 超时。
- 重试。
- 熔断。
- 降级。
- 幂等键。
- Trace 传播。
- Tenant 传播。
- 错误映射。

## 4. 禁止事项

- 在 common 中直接引入外部 SDK。
- 在框架中固定业务外部系统。
- 忽略超时和重试上限。
