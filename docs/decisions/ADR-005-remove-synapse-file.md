# ADR-005：删除 synapse-file

## Status

Accepted

## Context

文件存储即使抽象为 SPI，通常仍与文件元数据、权限、生命周期和对象存储策略共同演进，
这些能力需要可启动服务和平台治理。

## Decision

从 Framework 删除 `synapse-file`，不保留本地文件默认实现、文件元数据或 MinIO/S3
抽象。文件能力由 Synapse Platform 的可启动 `synapse-file-server` 承担。

## Consequences

Framework 不再提供文件存储客户端抽象；消费方需要使用 Platform 提供的 API/SDK 或
在业务应用内直接集成存储技术。现有模块文档和 Skill 必须移除有效模块描述。

## Rejected Alternatives

- 继续保留最小存储 SPI：仍会形成 Platform 文件能力的影子模型。
- 把 file server 放入 Framework：违反不可启动、不可承载平台服务的边界。
- 保留本地文件实现作降级：生产语义和安全边界不明确。

## Date

2026-06-19
