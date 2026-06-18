# synapse-config 设计说明

## 1. 模块使命

`synapse-config` 为 Framework 和消费方提供“读取字符串配置并解析为目标类型”的稳定端口，使配置来源可以替换，而调用代码不绑定 Nacos、数据库或某个配置中心 SDK。

## 2. 边界

负责：

- `ConfigClient` 原始配置读取端口。
- `ConfigParser` 字符串到类型的解析端口。
- `ConfigResolver` 类型化读取入口。
- `InMemoryConfigClient` 测试/轻量默认实现。
- Spring Boot 属性到本地 Map 的自动配置。

不负责：

- 配置发布、审批、灰度和版本管理。
- 配置数据库表、Controller、后台页面。
- 长连接推送、集群同步和配置中心服务端。
- Nacos 等产品 SDK 的强绑定。

## 3. 设计分层

```text
Config source adapter
  -> ConfigClient: Optional<String>
  -> ConfigParser: String -> T
  -> ConfigResolver: key + Class<T> -> Optional<T>
  -> consumer
```

将读取与解析分开后，同一个 parser 可以复用到内存、远程配置中心或数据库 adapter。

## 4. 核心对象角色

### 4.1 `ConfigClient`

只回答某个 key 是否存在及其原始字符串值。它不决定目标 Java 类型，也不隐藏远程失败语义。

### 4.2 `ConfigParser`

负责受控类型转换。默认支持常见基础类型和 `Duration`，不应通过任意反射把不可信字符串转换为复杂业务对象。

### 4.3 `ConfigResolver`

组合 Client 与 Parser，提供调用方的稳定入口。缺失配置与非法配置应保持不同语义：前者通常是 empty，后者应明确失败。

### 4.4 `InMemoryConfigClient`

用于单元测试、开发和轻量固定配置，不是生产配置中心。

## 5. 主链路

```text
consumer resolve(key, Boolean.class)
  -> ConfigClient.get(key)
  -> empty: Optional.empty
  -> value present: ConfigParser.parse
  -> typed value
```

## 6. 失败边界

- key 缺失：返回 empty，由消费方决定默认值或必填规则。
- value 存在但格式非法：明确抛出解析异常，不伪装成缺失。
- 远程 ConfigClient 失败：adapter 应保留可诊断异常，不能静默回退陈旧值，除非其缓存策略明确设计。
- 敏感值：不得出现在异常 message、日志或 metadata 示例中。

## 7. 扩展原则

- Nacos / DB / HTTP 来源：实现 `ConfigClient` adapter。
- 新类型：实现或装饰 `ConfigParser`。
- 缓存与刷新：放在 ConfigClient adapter 或后续专门组件，不把发布服务塞进本模块。
- 业务默认值由消费方定义，不由通用 Framework 猜测。

## 8. 自动配置原则

- 用户提供 `ConfigClient` / `ConfigParser` / `ConfigResolver` 时默认 Bean 退让。
- 本地 Map 只在没有真实 Client 时提供轻量实现。
- 配置属性必须生成 metadata，并说明 Map key/value 均为字符串。

## 9. 源码阅读顺序

```text
ConfigClient
  -> ConfigParser
  -> DefaultConfigParser
  -> ConfigResolver
  -> InMemoryConfigClient
  -> ConfigProperties
  -> ConfigAutoConfiguration
  -> parser tests
```

## 10. 手写练习

1. 写一个 `MapConfigClient`。
2. 写 parser 支持 Boolean、Duration。
3. 区分 key 缺失和 `"abc"` 解析 Integer 失败。
4. 写一个假的远程 Client，验证替换默认 Bean。

## 11. 修改检查清单

- 是否引入了配置中心服务端能力。
- 是否把业务配置模型写进 Framework。
- 是否把解析失败吞成 Optional.empty。
- 是否在日志中泄露敏感配置值。
- 新类型解析是否有明确格式和测试。
- AutoConfiguration 是否覆盖用户自定义 Bean。
