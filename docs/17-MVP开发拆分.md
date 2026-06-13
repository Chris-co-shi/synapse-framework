# 17-MVP开发拆分

## 1. MVP 目标

完成纯技术底座最小闭环。

## 2. 第一批模块

- synapse-bom。
- synapse-core。
- synapse-web。
- synapse-data。
- synapse-cache。
- synapse-security。
- synapse-audit。


## 3. 第二批模块

- synapse-tenant。
- synapse-data-permission。
- synapse-message。
- synapse-file。
- synapse-task。
- synapse-cloud。

## 4. 每个模块验收

- 自动配置启用和关闭测试。
- 用户自定义 Bean 覆盖测试。
- 异常语义测试。
- 公共 API 和复杂逻辑必要注释。
- 对应 Skill 更新。

## 5. 不进入 MVP

- 业务系统。
- 启动应用。
- 示例应用。
- Admin UI。
- 代码生成业务模板。
