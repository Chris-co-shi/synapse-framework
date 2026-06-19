# ADR-008：BOM 边界

## Status

Accepted

## Context

当前子模块重复导入 `synapse-bom`，且 BOM 引入 Spring Cloud Alibaba，仓库内部构建和
外部依赖管理职责混合。

## Decision

保留 `synapse-bom` 供外部项目导入，只管理 Spring Boot、Spring Cloud、Framework
直接使用的第三方依赖和正式发布的 Synapse JAR。删除 Alibaba BOM、Nacos、Seata、
Sentinel、SchedulerX、Platform 专用依赖和已删除模块。仓库内部依赖版本由根 Parent 的
`dependencyManagement` 继承，子模块不再导入 BOM；纯 POM 聚合模块不进入 BOM。

## Consequences

内部构建与外部消费职责清晰，减少重复配置；根 Parent 与外部 BOM 需要保持版本范围一致，
并由架构校验脚本检查正式 JAR 完整性。

## Rejected Alternatives

- 子模块继续导入 BOM：形成循环式职责和重复声明。
- BOM 管理所有生态依赖：把 Platform 技术选型扩散到 Framework。
- 将聚合 POM 加入 BOM：对消费方没有可用 JAR 价值。

## Date

2026-06-19
