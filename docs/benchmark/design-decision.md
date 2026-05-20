# 阶段 0 设计决策记录

## 1. 是否 Fork 一个开源框架作为主仓？

决策：不 Fork。

原因：

- 历史包袱重。
- 技术栈和包结构不可控。
- 容易被认为只是二次改名。
- 许可证和代码来源风险更高。
- 不利于形成自己的工程规范和 AI 协作规范。

采用方式：

```text
开源项目作为样本库
Synapse 作为干净主仓
Codex 只按规则实现，不复制源码
```

## 2. 是否第一版做微服务？

决策：不做。

原因：

- 当前目标是沉淀通用框架，不是展示微服务组件堆叠。
- 单体模块化更适合验证 IAM、RBAC、代码生成、数据权限等核心模型。
- 模块边界稳定后再拆微服务更安全。

## 3. 是否第一版做低代码？

决策：不做完整低代码，只做代码生成器。

原因：

- 低代码运行时复杂度极高。
- 第一版应优先保证生成代码可读、可测、可维护。
- 代码生成器是低代码之前的必要能力。

## 4. 是否使用 MyBatis-Plus？

决策：使用，但限制使用边界。

规则：

- MyBatis-Plus 只存在于 infrastructure persistence 层。
- Domain 不依赖 MyBatis-Plus。
- 不使用 ActiveRecord Model<T>。
- 不在 Controller 中直接使用 Mapper。
- 谨慎使用 IService / ServiceImpl，默认不作为业务 Service 基类。

## 5. 是否支持多租户？

决策：v0.1 预留，不完整实现。

预留内容：

- tenant_id 字段规则
- TenantContext
- TenantAware 接口
- MyBatis-Plus TenantLineInnerInterceptor 适配预留
- 租户隔离测试用例模板

## 6. 是否支持数据权限？

决策：v0.1 设计抽象，最小实现。

最小范围：

- 自己
- 本部门
- 本部门及子部门
- 自定义部门
- 全部数据

不做：

- 任意 SQL 片段拼接
- 用户可配置 SQL 条件
- 复杂表达式规则引擎

## 7. 是否前后端同仓？

决策：v0.1 可以同仓，模块隔离。

建议：

```text
synapse-admin-api
synapse-admin-ui
```

后续可拆仓。

## 8. 是否引入 DDD？

决策：采用轻量分层，不做重 DDD。

原则：

- 用 application/domain/infrastructure/interfaces 保持边界。
- 不为 CRUD 过度建模。
- 复杂业务才引入聚合、领域服务、领域事件。
- 系统管理模块以清晰分层优先。

## 9. v0.1 成功标准

- 能登录。
- 能刷新 token。
- 能登出。
- 能管理用户、角色、菜单。
- 能加载动态路由。
- 能做接口权限校验。
- 能记录操作日志和登录日志。
- 能跑 Flyway migration。
- 能生成一个标准 CRUD 模块。
- 能通过测试。
- Codex 能按规则持续开发而不破坏边界。
