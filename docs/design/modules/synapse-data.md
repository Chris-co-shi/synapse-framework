# synapse-data 设计说明

## 1. 模块使命

`synapse-data` 是 ORM 无关的数据语义抽象模块。

它只提供分页、排序、审计人提供者和通用字段名等 Java 契约，让 Web、MyBatis-Plus、业务层或其他适配模块可以共享数据层基础语义。

## 2. 边界

负责：

- `PageQuery` / `PageResult` 分页模型。
- `SortItem` / `SortDirection` 排序模型。
- `DataAuditorProvider` 审计人读取契约。
- `DataFieldNames` 通用数据字段名常量。

不负责：

- MyBatis-Plus 插件链、MetaObjectHandler、IdentifierGenerator。
- DataSource、连接池、多数据源清单、健康检查或路由。
- Flyway、Liquibase 或数据库 migration。
- BaseEntity、业务 Entity、Mapper、Repository、Service。
- 租户 SQL、数据权限、SQL 自动路由。
- Spring Boot AutoConfiguration。

## 3. 依赖方向

```text
synapse-core
  <- synapse-data
       <- synapse-mybatis-plus
       <- Web / Business modules
```

`synapse-data` 不依赖 MyBatis-Plus、dynamic-datasource、Spring Boot 或具体数据库驱动。

## 4. 核心对象

- `PageQuery`：外部页码、页大小和排序请求。
- `PageResult`：分页查询返回模型。
- `SortItem` / `SortDirection`：排序表达。
- `DataAuditorProvider`：当前数据审计人读取 Port。
- `DataFieldNames`：标准字段名常量。

## 5. 主链路

```text
controller / application service
  -> PageQuery / SortItem
  -> adapter module maps to concrete ORM page object
  -> query execution outside synapse-data
  -> PageResult
```

`synapse-data` 不直接参与 SQL 执行。

## 6. 生命周期与失败边界

- 分页模型只做结构表达，不做数据库访问。
- 最大分页限制由 Web 层、业务层或 ORM 适配层控制。
- 审计人缺失时由 `DataAuditorProvider` 返回空，不伪造 system。
- 字段名常量只是约定，不强制消费方实体继承。

## 7. 扩展原则

- ORM 适配放在独立模块，例如 `synapse-mybatis-plus`。
- 数据源治理放在 `synapse-datasource`。
- 业务项目可提供自己的 `DataAuditorProvider`。
- 不通过新增 BaseEntity 强迫所有消费方继承同一实体父类。

## 8. 源码阅读顺序

```text
page models
  -> sort models
  -> audit provider contract
  -> field names
  -> tests
```

## 9. 手写练习

1. 不看源码手写 `PageQuery` 和 `PageResult` 的最小版本。
2. 写一个排序白名单适配器，把 `SortItem` 转成 ORM 排序对象。
3. 写一个测试 `DataAuditorProvider`，无上下文时返回空。

## 10. 修改检查清单

- 是否新增了 MyBatis-Plus、dynamic-datasource 或 Spring Boot 依赖。
- 是否新增了业务 Entity、Mapper、Repository 或 migration。
- 是否把分页安全策略硬编码进基础模型。
- 是否默认伪造审计人为 system。
- 是否让 `synapse-data` 反向依赖 Web、Security 或具体 ORM。
