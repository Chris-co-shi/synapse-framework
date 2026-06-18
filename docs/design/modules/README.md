# Synapse Framework 模块设计文档

本目录按 Maven module 解释 Synapse Framework 的设计，而不是重复模块使用手册。

- 使用手册回答：如何引入、如何配置、如何调用。
- 设计文档回答：为什么存在、如何协作、边界在哪里、应该怎样阅读和修改源码。

## 推荐学习顺序

### 第一组：基础契约

1. [synapse-bom](synapse-bom.md)
2. [synapse-core](synapse-core.md)
3. [synapse-time](synapse-time.md)
4. [synapse-config](synapse-config.md)
5. [synapse-i18n](synapse-i18n.md)

### 第二组：Web 与调用链

6. [synapse-webmvc](synapse-webmvc.md)
7. [synapse-webflux](synapse-webflux.md)
8. [synapse-cloud](synapse-cloud.md)

### 第三组：安全与 OAuth2

9. [synapse-security](synapse-security.md)
10. [synapse-oauth2-core](synapse-oauth2-core.md)
11. [synapse-oauth2-authorization-server-support](synapse-oauth2-authorization-server-support.md)
12. [synapse-oauth2-resource-server-webmvc](synapse-oauth2-resource-server-webmvc.md)
13. [synapse-oauth2-resource-server-webflux](synapse-oauth2-resource-server-webflux.md)

### 第四组：基础设施能力

14. [synapse-data](synapse-data.md)
15. [synapse-cache](synapse-cache.md)
16. [synapse-audit](synapse-audit.md)
17. [synapse-file](synapse-file.md)
18. [synapse-mq](synapse-mq.md)

## 每份设计文档的固定结构

1. 模块使命：模块为什么存在。
2. 边界：明确负责和明确不负责的内容。
3. 依赖方向：它依赖谁，谁可以依赖它。
4. 核心对象：Contract、Model、Adapter、AutoConfiguration。
5. 主链路：一次真实调用如何经过关键对象。
6. 生命周期与失败边界：上下文、资源和异常如何收口。
7. 扩展原则：消费方应该替换哪个 Port 或 Bean。
8. 源码阅读顺序：避免按文件名盲读。
9. 手写练习：通过关闭源码重写验证理解。
10. 修改检查清单：防止破坏模块边界。

## 阅读方法

阅读一个模块时，先不要进入全部实现。按照以下顺序：

```text
模块设计文档
  -> 核心 Model / Port
  -> 默认实现
  -> 外部框架 Adapter
  -> AutoConfiguration
  -> Tests
```

修改任何类前，都要能够回答：

- 输入和输出是什么。
- 谁调用它。
- 失败由谁处理。
- 上下文或资源由谁清理。
- 为什么它属于当前模块。
- 消费方能否通过 Bean 或 Port 替换。

## 注释原则

源码注释重点解释：

- 非显然的职责边界。
- 调用顺序和框架生命周期。
- ThreadLocal / Reactor Context / InputStream 等资源清理要求。
- 默认实现为什么保守退让。
- 安全、幂等、缓存和消息能力的非目标。

不为以下内容增加噪声注释：

- getter / setter。
- 与方法名完全相同的描述。
- 简单条件判断。
- 仅重复 Java 语法的行内注释。
