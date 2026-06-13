# 15-API接口设计

## 1. 定位

Synapse Framework 不交付业务 API。本文档约束技术端点和消费方 API 建议。

## 2. 响应结构

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {},
  "traceId": "...",
  "timestamp": "2026-06-13T12:00:00Z"
}
```

## 3. 错误码分层

```text
COMMON_XXXX
WEB_XXXX
DATA_XXXX
CACHE_XXXX
SECURITY_XXXX
AUDIT_XXXX
TENANT_XXXX
MESSAGE_XXXX
FILE_XXXX
TASK_XXXX
CLOUD_XXXX
```

禁止在框架中定义业务错误码。

## 4. 技术端点

技术端点默认关闭或受保护：

- `/actuator/**`
- `/synapse/health`
- `/synapse/metadata`

禁止固定 `/api/admin`、`/api/system` 等业务路径。

## 5. OpenAPI

框架提供基础配置。消费方 Controller 自行补充业务接口说明。
