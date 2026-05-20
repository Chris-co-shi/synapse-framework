# JeecgBoot 对标分析

## 1. 项目定位

JeecgBoot 是低代码/零代码开发平台，公开说明强调：

- OnlineCoding
- Code Generator
- Manual Merge
- 在线表单
- 在线报表
- 报表配置
- 图表设计
- 大屏设计
- 移动端配置
- 表单设计器
- 流程设计
- 插件化能力

来源：https://github.com/jeecgboot/JeecgBoot

## 2. 值得借鉴

### 2.1 代码生成和手工合并模式

JeecgBoot 的关键思想不是“所有东西都低代码”，而是：

```text
简单功能：在线配置
复杂功能：生成代码 + 手工合并
```

Synapse 第一版应采用更克制的版本：

```text
表结构/元数据 -> 生成标准模块骨架 -> 人工二次开发 -> 测试闭环
```

### 2.2 元数据驱动

Synapse Codegen 应维护自己的元数据：

- 表名
- 字段名
- Java 类型
- DB 类型
- 是否列表展示
- 是否查询字段
- 是否表单字段
- 是否必填
- 是否唯一
- 是否字典
- 是否脱敏
- 是否租户字段
- 是否审计字段
- 是否逻辑删除
- 是否乐观锁

### 2.3 插件化能力

JeecgBoot 的插件思路值得长期参考。Synapse v0.1 只预留插件 SPI：

- ModuleContributor
- MenuContributor
- PermissionContributor
- MigrationContributor
- CodegenTemplateContributor

不要第一版实现完整插件市场。

## 3. 不建议照搬

### 3.1 第一版不要做完整低代码

完整低代码会引入大量元数据、运行时解释、页面设计器、权限融合和运行时调试成本。

Synapse v0.1 只做代码生成器，不做在线低代码运行时。

### 3.2 不要把魔法封装放进核心链路

框架核心必须保持透明：

- Controller 可读
- Application Service 可测
- Repository 可替换
- Mapper 可追踪
- SQL 可审计

### 3.3 不要过早做流程编排

工作流应作为 adapter，而不是框架核心。

## 4. 对 Synapse 的决策

采用：

- 元数据驱动代码生成
- 生成后手工合并
- 插件 SPI 预留
- 菜单/权限/前端路由自动生成思路

拒绝：

- 完整在线表单
- 完整低代码运行时
- 大屏设计器
- 第一版流程编排

## 5. Codex 使用建议

Codex 可以辅助生成代码生成器模板，但必须要求：

- 模板可审查
- 生成代码符合包结构
- 生成后必须能测试
- 生成逻辑不能直接操作业务数据库
