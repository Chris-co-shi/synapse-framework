# synapse-bom 设计说明

## 1. 模块使命

`synapse-bom` 解决的是“版本一致性”，不是“能力聚合”。它通过 Maven `dependencyManagement` 对齐 Synapse 内部模块和关键第三方依赖版本，使 Platform 与不同业务系统使用同一技术基线。

## 2. 边界

负责：

- 管理 `synapse-*` 模块版本。
- 管理 Framework 构建和运行所依赖的关键第三方版本。
- 为消费方提供可导入的 Maven BOM。

不负责：

- 自动引入任何模块。
- 启用 Spring Boot 自动配置。
- 充当 parent POM。
- 充当 starter。
- 管理业务系统专属 SDK 或数据库驱动选择。

## 3. 依赖关系

```text
Business / Platform parent pom
  -> import synapse-bom
  -> explicitly declare required synapse modules
```

BOM 中出现一个模块，只表示版本受管理，不表示该模块成为消费方依赖。

## 4. 核心设计决策

### 4.1 BOM 与 parent 分离

消费方不必继承 Framework parent，可保留自己的插件、发布和公司级构建规则，只导入 BOM 获取版本基线。

### 4.2 不创建 starter

Framework 按能力拆模块。消费方显式选择 `webmvc`、`data`、`security` 等模块，可以减少传递依赖和隐式行为。

### 4.3 第三方版本只管理关键基线

BOM 只应管理 Framework 公共技术栈。若将所有业务依赖都放入 BOM，会使 Framework 成为公司级“万能 parent”，破坏边界。

## 5. 失败模式

- 内部模块漏进 BOM：消费方必须重复声明版本，容易发生模块版本漂移。
- BOM 自动依赖模块：导入版本管理意外启用运行时能力。
- 业务依赖进入 BOM：不同业务系统被迫共享无关依赖版本。
- 消费方随意覆盖核心版本：可能产生二进制不兼容或自动配置行为差异。

## 6. 源码阅读顺序

该模块没有 Java 代码，按以下顺序阅读：

```text
synapse-bom/pom.xml
  -> root pom.xml properties
  -> each module pom.xml
  -> dependency convergence / build validation
```

重点检查：

- 内部模块版本是否统一使用项目版本属性。
- 第三方版本是否与 Spring Boot 依赖管理冲突。
- BOM 是否错误声明普通 dependencies，而不是 dependencyManagement。

## 7. 手写练习

关闭源码后写一个最小 BOM：

1. packaging 为 `pom`。
2. 在 `dependencyManagement` 管理两个内部模块。
3. 消费项目导入 BOM 后显式引入其中一个模块且不写版本。
4. 验证另一个模块不会被自动引入。

## 8. 修改检查清单

- 新增 Framework module 是否进入 BOM。
- 删除或改名 module 后是否清理旧坐标。
- 是否错误加入业务依赖。
- 是否把 BOM 描述成 starter。
- 是否在没有兼容性验证时升级关键第三方版本。
