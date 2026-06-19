# Synapse Framework 学习路径

本目录面向需要重新接管、理解和手写 Synapse Framework 代码的开发者。

这里不重复模块使用手册。模块手册回答“怎么接入”，本目录重点回答：

- 为什么这样拆模块。
- 一次请求如何穿过多个模块。
- 应该按什么顺序阅读源码。
- 哪些类是入口、适配器、契约和上下文边界。
- 如何验证自己是否真正理解，而不是只看懂代码表面。

## 1. 推荐阅读顺序

| 顺序 | 文档 | 学习目标 |
| --- | --- | --- |
| 1 | [Framework 架构阅读指南](01-framework-architecture-reading-guide.md) | 建立整体模块地图、依赖方向和核心概念 |
| 2 | [按模块设计文档](../design/modules/README.md) | 逐模块理解使命、边界、主链路、扩展点和源码阅读顺序 |
| 3 | [Security 与 OAuth2 请求链路](02-security-oauth2-request-flow.md) | 看懂 Bearer Token 到业务方法之间的完整认证链路 |
| 4 | [总体架构设计](../02-总体架构设计.md) | 理解 Framework / Platform / Business 的职责边界 |
| 5 | [核心链路设计](../03-核心链路设计.md) | 理解 OperationContext、Web、Security、Data、MQ 链路 |
| 6 | [模块使用手册](../modules/README.md) | 按模块查看配置、扩展点和接入方式 |

## 2. 每次阅读源码的固定方法

不要从目录第一行开始逐文件阅读。每次只追踪一条真实链路，并对每个关键类回答：

1. 它接收什么输入。
2. 它产生什么输出。
3. 谁调用它。
4. 它属于契约、适配器、默认实现还是自动配置。
5. 它失败时由谁转换异常或响应。
6. 它为什么属于当前模块，而不是相邻模块。

推荐使用下面的循环：

```text
先读设计文档
  -> 根据“源码阅读顺序”打开关键类
  -> 画出调用链
  -> 关闭源码复述职责
  -> 手写一个小类或测试
  -> 与现有实现对比
```

## 3. 学习时不要做的事

- 不要一次理解全部模块。
- 不要按文件名顺序逐个阅读。
- 不要把 Spring 自动配置、领域契约和业务实现混在一起理解。
- 不要只阅读实现而不看测试。
- 不要通过增加大量行内注释替代设计文档。
- 不要把 Framework 中不存在的业务能力想象成已实现功能。

## 4. 建议的阶段顺序

### 阶段一：上下文根

先掌握：

- `OperationContext`
- `OperationActor`
- `OperationContextHolder`
- `CurrentPrincipalContext`
- `SecurityOperationContextAdapter`

目标是理解为什么 data、audit、mq 不直接依赖 security。

### 阶段二：Servlet 请求链路

先掌握：

- `SynapseExceptionBridgeFilter`
- Trace / OperationContext 恢复
- Spring Security Resource Server
- `SynapsePrincipalContextBridgeFilter`
- `GlobalExceptionHandler`

目标是能够解释 Filter 阶段异常与 Controller 阶段异常的处理边界。

### 阶段三：认证与权限

先掌握：

- `AuthenticatedPrincipal`
- `AuthenticatedUser`
- `AuthenticatedClient`
- `PermissionChecker`
- `@RequirePermission`

目标是能够区分认证主体、Spring Authentication、权限快照和业务授权模型。

### 阶段四：基础设施适配

按需要阅读：

- data 自动填充
- cache / lock / idempotency
- mq 上下文传播
- file storage port
- cloud / Feign 上下文传播
- time / timezone 查询范围转换

## 5. 理解完成的判定标准

一个模块只有在满足下面条件时才算已经掌握：

- 能用三句话说明模块定位和明确不做什么。
- 能画出主要输入到输出的调用链。
- 能解释与相邻模块的依赖方向。
- 能指出至少一个扩展点和一个默认实现。
- 能关闭源码写出一个关键接口、适配器或测试。
- 能说明失败路径和上下文清理位置。

学习目标不是记住所有代码，而是建立稳定的结构认知，使后续修改代码时知道应该改哪里、不能改哪里。
