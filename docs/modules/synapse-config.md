# synapse-config 使用手册

## 1. 模块定位

`synapse-config` 提供配置抽象、运行时读取和类型解析能力。

当前能力：

- `ConfigClient`：配置读取端口。
- `ConfigParser`：配置字符串到目标类型的解析端口。
- `ConfigResolver`：类型化配置解析入口。
- `InMemoryConfigClient`：轻量本地 Map 默认实现。
- Spring Boot 自动配置。

## 2. 适用场景

- framework 或业务系统需要统一读取运行时配置。
- 消费方需要适配自己的配置来源。
- 测试或轻量场景通过本地 Map 提供配置值。

## 3. 不适用场景

- config-service。
- 配置中心后台。
- 配置发布、审批、灰度。
- 配置数据库表。
- 可启动配置服务。

## 4. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-config</artifactId>
</dependency>
```

## 5. 快速使用

```java
Optional<Boolean> enabled = resolver.resolve("feature.enabled", Boolean.class);
```

默认解析支持 `String`、`Integer`、`Long`、`Boolean`、`Double`、`Duration`。

## 6. 配置项

```yaml
synapse:
  config:
    values:
      feature.enabled: "true"
```

## 7. 扩展方式

消费方可以提供自定义 `ConfigClient`、`ConfigParser` 或 `ConfigResolver` Bean，自动配置不会覆盖。

## 8. 边界

- 只做配置抽象和客户端能力。
- 不持久化配置，不建表。
- 不提供 Controller 或后台。
- 不做 Synapse Platform 的 `synapse-config-service`。
