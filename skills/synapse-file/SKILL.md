# synapse-file Skill

## 职责

`synapse-file` 只提供文件存储技术抽象、保存命令、文件对象模型、读取结果和轻量本地存储实现。

## 禁止事项

- 不做 file-service。
- 不新增上传/下载 Controller。
- 不新增附件表、文件权限、文件中心后台。
- 不实现预签名 URL、预览、转码、OCR、水印、CDN 等平台能力。
- 不新增业务 Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 存储端口使用 `FileStorage`。
- 保存请求使用 `StoreFileCommand`。
- 默认实现只能是本地轻量实现。
- OSS、S3、MinIO 等应作为后续 adapter 或消费方实现接入。

## 测试要求

- 覆盖文件保存和读取。
- 覆盖非法 bucket / objectKey。
- 覆盖自动配置和自定义 Bean 不覆盖。
- 覆盖本地存储路径边界。
- 覆盖 `synapse.file.*` Spring Boot Configuration Metadata。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-file.md`
- `docs/phase-2/00-framework-boundary.md`
