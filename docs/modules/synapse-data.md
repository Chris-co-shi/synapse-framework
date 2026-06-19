# synapse-data 使用手册

## 1. 模块定位

`synapse-data` 是 ORM 无关的数据语义抽象模块。

它只提供分页模型、排序模型、审计人提供者和通用数据字段名等基础概念。

## 2. 当前事实

当前模块提供：

- `PageQuery`
- `PageResult`
- `SortItem`
- `SortDirection`
- `DataAuditorProvider`
- `DataFieldNames`

## 3. 明确不提供

`synapse-data` 不提供：

- MyBatis-Plus 依赖。
- dynamic-datasource 依赖。
- Flyway 依赖。
- Spring Boot AutoConfiguration。
- BaseEntity。
- Mapper。
- Repository 实现。
- DataSource 配置。
- Flyway migration。
- MetaObjectHandler。
- MyBatis-Plus IdentifierGenerator。
- 租户字段。
- 数据权限。
- 动态数据源。
- SQL 路由。

## 4. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-data</artifactId>
</dependency>
```

## 5. 分页模型

```java
PageQuery query = PageQuery.of(1, 20, List.of(SortItem.desc("createdAt")));
PageResult<String> result = PageResult.of(List.of("a"), 1, 1, 20);
```

注意：

- `PageQuery` 不做最大分页限制。
- `PageQuery` 不修正负数页码。
- 分页安全限制由 Web 层、业务层或 `synapse-mybatis-plus` 适配层负责。

## 6. 边界说明

MyBatis-Plus 工程增强属于 `synapse-mybatis-plus`。

多数据源、数据库类型识别、健康检查、Load Balance、Router、Failover / Fail-fast 等数据源治理能力属于 `synapse-datasource`。
