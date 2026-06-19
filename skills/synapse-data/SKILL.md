# synapse-data Skill

## 职责

`synapse-data` 只提供 ORM 无关的数据语义抽象。

## 允许内容

- 分页模型。
- 排序模型。
- 审计字段名。
- 通用数据字段名。
- ORM 无关数据语义接口。

## 禁止事项

- 不引入 MyBatis-Plus。
- 不引入 dynamic-datasource。
- 不引入 Flyway。
- 不新增 Spring Boot AutoConfiguration。
- 不新增 BaseEntity。
- 不新增 Mapper、Repository、Service。
- 不新增 DataSource 配置。
- 不新增租户字段、数据权限或 SQL 路由。
- 不新增业务 Entity、业务表结构或业务 migration。
- 不新增 Controller 或可启动服务。
- 不创建 starter、demo、example、sample application。

## 验证

- 搜索 `com.baomidou`、`org.flywaydb`、`AutoConfiguration`。
- 确认 `synapse-data` 不包含 MyBatis-Plus、Flyway、dynamic-datasource、Spring Boot 自动配置。
