# Synapse Framework 重构基线

本文档记录整体架构重构开始前的仓库事实。统计基于 `main` 分支提交
`0494814`，盘点日期为 2026-06-19。本文中的“当前”均指 Phase 0 开始时，
目标结构以 `docs/decisions` 中的 ADR 为准。

## 1. 工程基线

| 项目 | 当前值 |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.15 |
| Framework 版本 | 0.1.0-SNAPSHOT |
| 根包名 | `com.indigo.synapse` |
| Maven project 数 | 21（根工程加 20 个子模块） |
| 生产 Java 文件 | 379 |
| 测试 Java 文件 | 140 |

Phase 0 开始时工作区无未提交变更。基线测试 `mvn clean test` 已通过。

## 2. Reactor 模块

根 `pom.xml` 中的模块顺序如下：

```text
synapse-bom
synapse-core
synapse-webmvc
synapse-webflux
synapse-cloud
synapse-time
synapse-config
synapse-i18n
synapse-data
synapse-mybatis-plus
synapse-datasource
synapse-cache
synapse-security
synapse-oauth2-core
synapse-oauth2-authorization-server-support
synapse-oauth2-resource-server-webmvc
synapse-oauth2-resource-server-webflux
synapse-audit
synapse-file
synapse-mq
```

已确认的目标变化：

- `synapse-webmvc`、`synapse-webflux` 移入 `synapse-web` 聚合，并新增
  `synapse-web-core`。
- OAuth2 模块移入 `synapse-oauth2` 聚合，新增 client、resource-server
  聚合和 resource-server core。
- 删除 `synapse-cloud`、`synapse-file`。
- `synapse-mq` 更名为 `synapse-messaging`。
- 新增 `synapse-observability`、`synapse-resilience`。

## 3. BOM 基线

`synapse-bom` 当前导入：

- `spring-boot-dependencies` 3.5.15。
- `spring-cloud-dependencies` 2025.0.2。
- `spring-cloud-alibaba-dependencies` 2025.0.0.0。

当前直接管理的主要第三方版本：

- MyBatis-Plus 3.5.16。
- dynamic-datasource 4.3.1。
- JUnit 5.10.3。

当前 BOM 管理全部 19 个非 BOM 子模块，包括未来将删除或更名的
`synapse-cloud`、`synapse-file`、`synapse-mq`。各子模块又重复导入
`synapse-bom`，仓库内部依赖管理与外部消费 BOM 的职责尚未分离。

Phase 1 将删除 Spring Cloud Alibaba BOM 和相关版本属性；纯聚合 POM 不进入
BOM，正式 JAR 模块由 BOM 管理，仓库内部版本由根 Parent 统一管理。

## 4. 模块依赖方向

当前 Synapse 内部主要依赖如下：

```text
synapse-core
├── synapse-webmvc
├── synapse-webflux
├── synapse-cloud
│   └── synapse-oauth2-core
├── synapse-time
├── synapse-config
├── synapse-i18n
├── synapse-mybatis-plus
│   └── synapse-data
├── synapse-cache
├── synapse-security
├── synapse-oauth2-core
├── synapse-audit
├── synapse-file
└── synapse-mq

synapse-oauth2-core
└── synapse-oauth2-authorization-server-support

synapse-security + synapse-oauth2-core + synapse-webmvc
└── synapse-oauth2-resource-server-webmvc

synapse-security + synapse-oauth2-core + synapse-webflux
└── synapse-oauth2-resource-server-webflux
```

`synapse-data` 和 `synapse-datasource` 当前没有 Synapse 运行时模块依赖。
`synapse-cloud` 对 `synapse-oauth2-core` 的依赖将在删除 cloud 模块时消失。

## 5. 自动配置类

当前共有 19 个 `@AutoConfiguration` 类：

| 模块 | 自动配置 |
| --- | --- |
| audit | `SynapseAuditAutoConfiguration` |
| cache | `SynapseCacheAutoConfiguration` |
| cloud | `SynapseCloudAutoConfiguration`、`SynapseFeignAutoConfiguration` |
| config | `SynapseConfigAutoConfiguration` |
| datasource | `SynapseDatasourceAutoConfiguration` |
| file | `SynapseFileAutoConfiguration` |
| i18n | `SynapseI18nAutoConfiguration` |
| mq | `SynapseMqAutoConfiguration` |
| mybatis-plus | `SynapseMybatisPlusAutoConfiguration` |
| oauth2 authorization | `SynapseAuthorizationServerSupportAutoConfiguration` |
| oauth2 resource mvc | `SynapseResourceServerWebMvcAutoConfiguration` |
| oauth2 resource webflux | `SynapseResourceServerWebFluxAutoConfiguration` |
| security | `SynapseSecurityAutoConfiguration` |
| time | `SynapseTimeAutoConfiguration` |
| webmvc | `SynapseWebAutoConfiguration`、`SynapseWebErrorAutoConfiguration`、`SynapseWebMvcAutoConfiguration` |
| webflux | `SynapseWebFluxAutoConfiguration` |

## 6. AutoConfiguration.imports

下列模块存在
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
synapse-audit
synapse-cache
synapse-cloud
synapse-config
synapse-datasource
synapse-file
synapse-i18n
synapse-mq
synapse-mybatis-plus
synapse-oauth2-authorization-server-support
synapse-oauth2-resource-server-webmvc
synapse-oauth2-resource-server-webflux
synapse-security
synapse-time
synapse-webflux
synapse-webmvc
```

每个 imports 文件当前均指向其模块内已存在的自动配置类。后续移动或更名模块时
必须同步迁移资源路径和类全名。

## 7. Configuration Properties

当前共有 12 个公开 `@ConfigurationProperties` 类型：

```text
SynapseCacheProperties
SynapseCloudProperties
SynapseFeignProperties
SynapseConfigProperties
SynapseDatasourceProperties
SynapseFileProperties
SynapseI18nProperties
SynapseMybatisPlusProperties
SynapseJwtSigningProperties
SynapseResourceServerProperties
SynapseSecurityProperties
SynapseTimeProperties
```

Phase 0 不改变配置前缀或默认值。后续公开配置变化必须同步字段 Javadoc、模块手册、
Skill 和生成的 `spring-configuration-metadata.json`。

## 8. 文档与 Skill 基线

当前文档分布在：

- 根 `README.md`。
- `docs/phase-2`、`docs/phase-3`。
- `docs/modules` 和 `docs/design/modules`。
- `docs/learning`、`docs/migration`。
- `skills/<module>/SKILL.md`。

现有 README、模块索引、设计文档、学习文档和 Skill 广泛引用
`synapse-cloud`、`synapse-file`、`synapse-mq`。这些引用在 Phase 0 是当前事实，
Phase 1 起必须随模块删除和更名同步更新，不能留下仍可消费的错误描述。

## 9. 已识别的架构债务

1. Web 和 OAuth2 尚未形成明确的聚合层与共享 core 层。
2. `SecurityContext` 等名称容易与 Spring Security 类型混淆。
3. MVC/WebFlux Resource Server 的共享验证语义仍分散在适配模块。
4. Framework 仍包含已决策删除的 cloud 和 file 能力。
5. 消息模块名称和职责未对齐 broker-neutral messaging 目标。
6. 子模块重复导入 BOM，Alibaba BOM 超出 Framework 目标边界。
7. WebMVC 自动配置创建全局 `ObjectMapper` 的风险需要专项修复。
8. 自动配置条件、用户 Bean 覆盖和可选依赖行为缺少统一契约测试。
9. Observability、Resilience 尚无独立技术边界。
10. 架构、README、BOM 和模块目录之间缺少自动一致性校验。

## 10. Phase 0 验证

执行命令：

```bash
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean verify
git diff --check
```

三个命令均通过，Maven reactor 的 21 个 project 全部成功。构建存在已有的 deprecated
API 编译告警，但没有测试失败。Phase 0 只建立事实和决策基线，不修改 Java 运行逻辑、
POM、依赖或模块结构。
