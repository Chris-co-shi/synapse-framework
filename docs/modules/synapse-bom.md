# synapse-bom 使用手册

## 1. 模块定位

`synapse-bom` 是 Synapse Framework 的 Maven BOM 模块。

它只负责统一管理 Synapse Framework 一阶段模块和关键第三方依赖的版本，方便业务系统、平台系统或其他内部模块通过 `dependencyManagement` 进行版本对齐。

它不包含 Java 代码，不提供自动配置，也不是 starter。

## 2. 适用场景

业务系统或平台系统在以下场景应该引入 `synapse-bom`：

- 需要统一 Synapse Framework 各模块版本。
- 需要避免每个 `synapse-*` 依赖都手写版本号。
- 需要统一 Spring Boot、MyBatis-Plus、JUnit 等基础依赖版本。
- 需要多个业务系统使用同一套 framework 版本基线。
- 需要平台服务和业务服务依赖版本保持一致。

## 3. 不适用场景

`synapse-bom` 不适合承担以下职责：

- 不提供 Java 类。
- 不提供 Spring Boot 自动配置。
- 不提供启动器能力。
- 不会自动引入任何 `synapse-*` 模块。
- 不会自动启用 web、security、data、cache、file、mq 等能力。
- 不管理业务系统自己的依赖选择。
- 不替代业务系统的父 POM。

如果业务系统需要某个具体能力，必须显式引入对应模块。

## 4. Maven 引入

业务系统推荐在 `dependencyManagement` 中导入：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

然后按需引入具体模块，不需要再写版本号：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>
```

## 5. 当前管理内容

### 5.1 Java 和基础版本

当前 BOM 基线：

| 项 | 版本 |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.15 |
| MyBatis-Plus | 3.5.16 |
| dynamic-datasource | 4.3.1 |
| JUnit Jupiter | 5.10.3 |
| Jakarta Validation API | 3.0.2 |

### 5.2 一阶段内部模块

当前 BOM 管理以下 Synapse Framework 模块版本：

```text
synapse-core
synapse-webmvc
synapse-webflux
synapse-cloud
synapse-data
synapse-cache
synapse-security
synapse-security-webmvc
synapse-oauth2-core
synapse-oauth2-authorization-server-support
synapse-oauth2-resource-server-webmvc
synapse-oauth2-resource-server-webflux
synapse-audit
synapse-file
synapse-mq
```

说明：

- BOM 只管理版本。
- 不会自动引入这些模块。
- 业务系统必须按需显式声明依赖。

## 6. 快速使用

### 6.1 只使用 Web 基础能力

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>
```

### 6.2 使用 Web + Security

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>

<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-security</artifactId>
</dependency>
```

### 6.3 使用 Data 自动填充

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-data</artifactId>
</dependency>
```

### 6.4 使用 MQ 契约

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-mq</artifactId>
</dependency>
```

## 7. 扩展方式

### 7.1 业务系统覆盖第三方版本

如果业务系统确实需要覆盖某个第三方依赖版本，可以在自己的 `dependencyManagement` 中声明更靠后的版本管理。

示例：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- 业务系统确有必要时再覆盖 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${custom.junit.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

覆盖前应评估是否会破坏 framework 模块测试基线。

### 7.2 平台项目统一版本

平台项目可以在自己的父 POM 中导入 `synapse-bom`，然后各微服务只声明需要的模块。

这样可以保证 gateway、iam、file、mq、task 等平台服务使用同一套 framework 版本。

## 8. 配置项

`synapse-bom` 没有运行时配置项。

它只在 Maven 构建期生效。

## 9. 边界与注意事项

### 9.1 BOM 不是 starter

导入 BOM 不会自动启用任何能力。

错误理解：

```text
导入 synapse-bom = 自动拥有 web/security/data/cache 等能力
```

正确理解：

```text
导入 synapse-bom = 统一版本
显式引入 synapse-webmvc = 使用 web 能力
显式引入 synapse-webflux = 使用 WebFlux 技术支撑能力
显式引入 synapse-security = 使用 security 能力
```

### 9.2 BOM 不应该引入业务依赖

`synapse-bom` 不应该管理业务系统专属依赖，例如业务 SDK、业务数据库驱动版本、业务前端依赖等。

### 9.3 BOM 不应该承载模块边界逻辑

模块是否依赖另一个模块，由具体模块 POM 决定。BOM 只管理版本，不表达实际依赖关系。

### 9.4 不要把所有模块都一次性引入业务系统

业务系统应按需引入。一次性引入所有模块会增加依赖面，也容易让模块边界变得不清晰。

## 10. 常见问题

### Q1：导入 synapse-bom 后，为什么找不到 `Result`？

因为 BOM 只做版本管理。还需要显式引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>
```

### Q2：业务系统是否必须以 synapse-framework 作为 parent？

不必须。推荐通过 `dependencyManagement` import BOM，而不是强制继承 parent。

### Q3：BOM 中管理了 `synapse-mq`，是不是业务系统会自动引入 MQ？

不会。只有显式声明 `synapse-mq` 依赖时才会引入。

### Q4：BOM 会管理 starter 吗？

不会。本项目不创建 `synapse-starter-*`，BOM 只管理当前 framework module 和必要第三方依赖版本。

### Q5：为什么 BOM 中有第三方依赖版本？

为了让 framework 内部模块和消费方在关键基础依赖上保持一致，降低版本冲突风险。
