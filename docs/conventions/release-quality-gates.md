# 发布与质量门禁

## 1. 版本和构件

- 使用语义化版本 `MAJOR.MINOR.PATCH`；开发分支可以使用 `-SNAPSHOT`。
- 已发布版本不可覆盖。发布前先将根 POM 设置为非 SNAPSHOT，再创建完全匹配的 `v<version>` Tag。
- 每个正式 JAR 生成 main、sources 和 javadoc 构件。
- 发布命令不得使用 `-DskipTests`；Release 工作流显式传入 `-DskipTests=false`。

## 2. 本地和 PR 门禁

`mvn clean verify` 执行：

- 编译和单元/集成测试。
- 架构与文档一致性校验。
- Maven Enforcer 环境和 POM 检查。
- 轻量 Checkstyle 源码卫生检查。
- SpotBugs High 级问题阻断。
- JaCoCo 报告生成。
- sources 和 javadoc 构件生成。

PR 工作流执行同一命令，避免本地与 CI 使用不同验收标准。

## 3. API 兼容报告

0.x 阶段 API 兼容只生成报告，不默认阻断。已有可解析的历史版本后执行：

```bash
mvn clean verify -Papi-compatibility -Dapi.baseline.version=0.1.0
```

接近 1.0 时再评估将二进制不兼容升级为阻断规则。Configuration Properties、配置前缀、SPI、异常类型和
AutoConfiguration 同样属于兼容性评审范围，不能只看 class/method 报告。

## 4. 安全扫描

OWASP Dependency Check 在每周和人工触发工作流运行，避免每次本地构建下载漏洞数据库。
发现高危漏洞后应评估可利用性、升级路径和兼容影响，不得仅通过提高阈值隐藏问题。

## 5. 发布步骤

1. 更新版本和 `CHANGELOG.md`，确认非 SNAPSHOT。
2. 执行 `mvn clean verify` 和 `python3 scripts/verify-architecture.py`。
3. 创建 `v<version>` Tag；工作流校验 Tag 与 POM 完全一致。
4. Release 工作流执行 Maven deploy 并创建 GitHub Release。
5. 验证 GitHub Packages 中 main/sources/javadoc 构件完整且版本未被覆盖。
