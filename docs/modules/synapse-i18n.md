# synapse-i18n 使用手册

## 1. 模块定位

`synapse-i18n` 提供国际化运行时消息解析抽象。

当前能力：

- `LocaleResolver`：当前调用方 Locale 解析端口。
- `I18nResourceLoader`：国际化资源加载端口。
- `I18nMessageResolver`：消息解析和格式化入口。
- `InMemoryI18nResourceLoader`：轻量本地 Map 默认实现。
- Spring Boot 自动配置。

## 2. 适用场景

- 根据 Locale 解析错误消息或通用文案。
- 消费方需要接入自己的资源来源。
- framework 模块需要统一国际化解析端口。

## 3. 不适用场景

- i18n-resource-center。
- 翻译审批。
- 语言维护后台。
- 资源发布服务。
- 可启动国际化资源中心。

## 4. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-i18n</artifactId>
</dependency>
```

## 5. 快速使用

```java
Optional<String> message = resolver.resolve("hello", Locale.forLanguageTag("zh-CN"), "Synapse");
```

默认使用 `MessageFormat` 格式化参数。

## 6. 配置项

```yaml
synapse:
  i18n:
    default-locale: zh-CN
    messages:
      zh-CN:
        hello: "你好，{0}"
```

## 7. 扩展方式

消费方可以提供自定义 `LocaleResolver`、`I18nResourceLoader` 或 `I18nMessageResolver` Bean。

## 8. 边界

- 不提供资源中心服务。
- 不提供 Controller、Entity、Mapper、migration。
- 不做翻译工作流或后台管理。
- 不做 Synapse Platform 的 `synapse-i18n-resource-center`。
