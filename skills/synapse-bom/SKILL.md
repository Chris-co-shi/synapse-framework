# synapse-bom Skill

## 职责

`synapse-bom` 只负责 dependencyManagement 版本管理和 Synapse 内部模块版本声明。

## 禁止事项

- 不写 Java 代码。
- 不提供自动配置。
- 不做 starter 聚合包。
- 不引入业务依赖。
- 不把 BOM 当成可启动能力。

## 修改规则

- 新增 reactor module 时必须在 BOM 中声明对应 artifact 版本。
- 第三方版本只在确有必要时新增，并说明影响范围。
- BOM 管理版本不代表消费方一定会实际引入该依赖。

## 验证

- `mvn -q validate`
- 检查 root reactor 与 BOM 内部模块声明一致。
