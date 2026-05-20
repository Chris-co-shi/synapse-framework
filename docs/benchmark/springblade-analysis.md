# SpringBlade 对标分析

## 1. 项目定位

SpringBlade 是 Spring Cloud 分布式微服务架构和 Spring Boot 单体式微服务架构并存的综合型项目，公开说明中包含 Java17、Spring Boot 3.5、Spring Cloud 2025、Spring Cloud Alibaba、Nacos、MyBatis Plus 等技术栈，并提供 React 和 Vue 前端框架用于搭建企业级 SaaS 多租户微服务平台。

来源：https://github.com/chillzhuang/SpringBlade

## 2. 值得借鉴

### 2.1 单体与微服务双形态

Synapse 应采用：

```text
v0.1：单体模块化
v0.2：模块边界稳定
v0.3：可拆微服务
v0.4：提供 Cloud 版本
```

不要第一版直接微服务化。

### 2.2 SaaS 多租户意识

即使 v0.1 不做完整 SaaS，也要提前考虑：

- tenant_id
- tenant package
- tenant context
- tenant ignore
- 租户级参数配置
- 租户级菜单/权限扩展

### 2.3 技术基线现代化

SpringBlade 当前采用较新的 Spring Boot/Spring Cloud 技术栈。Synapse 应避免从老项目继承过时栈。

## 3. 不建议照搬

- 不要直接进入微服务治理。
- 不要第一版引入 Nacos、Gateway、OpenFeign、Sentinel、Seata 全家桶。
- 不要让企业 SaaS 能力污染 v0.1 的最小后台框架。

## 4. 对 Synapse 的决策

采用：

- 单体优先，微服务可演进
- SaaS 多租户预留
- Java 21 / Spring Boot 3.x 现代技术基线

拒绝：

- 第一版微服务化
- 第一版引入全部 Spring Cloud 组件
- 直接复刻商业化平台设计
