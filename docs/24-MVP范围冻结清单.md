# 24-MVP范围冻结清单

## 1. MVP 内

- BOM。
- Common。
- Web。
- Data。
- Cache。
- Security。
- Audit。
- Starter。
- Tenant 抽象。
- Data Permission 抽象。
- Message 抽象。
- File 抽象。
- Task 抽象。
- Cloud 抽象。

## 2. MVP 外

- 业务系统。
- Admin UI。
- IAM 业务实现。
- 示例应用。
- 代码生成业务模板。
- 低代码运行时。
- 完整微服务治理平台。

## 3. 冻结规则

新增范围必须先更新本文档，并说明：

- 为什么属于技术底座。
- 为什么不能放到消费方项目。
- 是否新增依赖。
- 是否影响 starter 默认行为。
