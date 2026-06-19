# synapse-mybatis-plus Skill

## 职责

`synapse-mybatis-plus` 提供 MyBatis-Plus 工程增强。

## 允许内容

- MyBatis-Plus starter 接入。
- JSqlParser 接入。
- MyBatis-Plus 自动配置。
- `MybatisPlusInterceptor`。
- 分页、乐观锁、防全表更新删除、非法 SQL 插件开关。
- 自动字段填充。
- OperationContext 到 DataAuditorProvider 的审计适配。
- MyBatis-Plus ID 生成适配。
- `synapse-data` 分页模型与 MyBatis-Plus `Page` 转换。

## 禁止事项

- 不新增业务 Entity、Mapper、Repository、Service。
- 不新增业务表结构或业务 migration。
- 不做 DataSource 治理。
- 不实现 SQL 自动读写路由。
- 不引入 Seata。
- 不做应用层主库晋升。
- 不新增 Controller 或可启动服务。

## 验证

- 确认 MyBatis-Plus 相关自动配置只存在于本模块。
- 确认实体示例只出现在文档或测试中，不作为生产业务模型。
