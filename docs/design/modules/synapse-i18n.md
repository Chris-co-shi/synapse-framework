# synapse-i18n 设计说明

## 1. 模块使命

`synapse-i18n` 提供运行时消息解析契约，将“当前 Locale、资源来源、模板格式化”分离，使 Framework 与业务代码不绑定某个资源中心或固定 properties 文件。

## 2. 边界

负责：

- `LocaleResolver` 当前 Locale 解析端口。
- `I18nResourceLoader` 文案资源读取端口。
- `I18nMessageResolver` 模板查找与参数格式化入口。
- `InMemoryI18nResourceLoader` 轻量默认实现。
- 默认 Locale 和本地消息自动配置。

不负责：

- 翻译审批、发布和版本管理。
- 语言资源中心服务端。
- 数据库表、管理 API 和后台页面。
- 自动翻译、机器翻译和业务文案治理。

## 3. 设计分层

```text
request / user preference
  -> LocaleResolver
resource adapter
  -> I18nResourceLoader
message code + locale + arguments
  -> I18nMessageResolver
  -> formatted message
```

Locale 的来源和资源的来源是两个独立变化点，因此分别定义 Port。

## 4. 核心对象角色

### 4.1 `LocaleResolver`

解析当前调用方 Locale。默认 Locale 只是明确配置的兜底，不应依赖服务器系统 Locale。

### 4.2 `I18nResourceLoader`

按 locale 与 code 返回原始模板。数据库、Redis、远程资源中心或本地 Map 都通过该端口适配。

### 4.3 `I18nMessageResolver`

组合资源查找和 `MessageFormat` 参数格式化。它不决定错误码，也不负责翻译流程。

### 4.4 `InMemoryI18nResourceLoader`

适合测试、默认通用文案和小规模静态资源，不是资源中心。

## 5. 主链路

```text
resolve(messageCode, optional locale, args)
  -> determine explicit/current/default Locale
  -> I18nResourceLoader.load(locale, code)
  -> template found
  -> MessageFormat with same Locale
  -> Optional<String>
```

## 6. 失败与回退边界

- code 缺失：返回 empty 或由上层决定 fallback code，不应伪造翻译。
- locale 缺失：使用显式配置的 default locale，而不是 JVM 默认值。
- 参数数量错误：应保留可诊断失败，避免静默输出错误文案。
- 多级语言回退（如 zh-CN -> zh）如果加入，必须明确顺序并测试。
- 日志不要记录可能包含敏感业务参数的完整格式化内容。

## 7. 扩展原则

- 用户偏好 Locale：替换 `LocaleResolver`。
- 资源中心：实现 `I18nResourceLoader`，可在 adapter 内做缓存。
- 自定义模板引擎：替换 `I18nMessageResolver`，但保持 code/locale 语义稳定。
- Framework 只提供通用错误文案；业务文案由业务系统或 Platform 管理。

## 8. 自动配置原则

- 用户 Bean 优先。
- 默认 Locale 使用 BCP 47 / Java Locale 可解析格式。
- nested messages Map 只作为轻量本地实现。
- metadata 说明 Locale 格式和消息 Map 结构。

## 9. 源码阅读顺序

```text
LocaleResolver
  -> I18nResourceLoader
  -> InMemoryI18nResourceLoader
  -> I18nMessageResolver
  -> default implementation
  -> I18nProperties
  -> I18nAutoConfiguration
  -> locale / formatting tests
```

## 10. 手写练习

1. 为 `zh-CN` 和 `en-US` 放入两个 `hello` 模板。
2. 使用相同 code 和不同 Locale 解析。
3. 验证参数通过对应 Locale 格式化。
4. 自定义 ResourceLoader 模拟远程资源并替换默认 Bean。

## 11. 修改检查清单

- 是否把资源中心服务端放入 Framework。
- 是否依赖 JVM 默认 Locale。
- 是否混入业务翻译流程或审批语义。
- 缺失资源和格式化失败是否被混为一类。
- 缓存是否属于 adapter 而不是污染核心 Port。
- 配置 metadata 是否与实际结构一致。
