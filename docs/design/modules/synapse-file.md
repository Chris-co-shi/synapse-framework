# synapse-file 设计说明

## 1. 模块使命

`synapse-file` 定义文件内容存储的最小 SPI，并提供开发/测试可用的本地文件系统实现。它让业务和 Platform 文件服务不直接绑定 MinIO、S3、OSS 或本地磁盘。

## 2. 边界

负责：

- `FileStorage` store/load/delete SPI。
- `StoreFileCommand`。
- `FileObject` 存储元数据。
- `StoredFile` 内容流结果。
- `LocalFileStorage`。
- 本地根目录自动配置。

不负责：

- 上传下载 Controller。
- 业务附件表和关联关系。
- 文件权限、访问 URL、预签名 URL。
- 预览、转码、OCR、水印、病毒扫描。
- 对象存储完整 SDK 封装。

## 3. 存储对象与业务附件分离

```text
FileStorage object
  -> bucket + objectKey + content metadata

Business attachment
  -> attachmentId + businessId + purpose + uploader + permissions
```

Framework 只处理第一部分。业务附件可以引用 bucket/objectKey，但不能反向污染存储 SPI。

## 4. 核心对象角色

### 4.1 `StoreFileCommand`

包含 bucket、objectKey、contentType 和 InputStream。调用方负责输入流生命周期；实现读取内容时不应擅自保留调用方流引用。

### 4.2 `FileObject`

只表达存储层元数据：bucket、objectKey、contentType、size。它不是数据库 Entity，也不包含业务名称、创建人和权限。

### 4.3 `StoredFile`

组合 metadata 与输出 InputStream。调用方必须关闭返回流，因此 API 文档和 Javadoc 必须明确资源所有权。

### 4.4 `LocalFileStorage`

映射：

```text
rootDirectory / bucket / objectKey
```

负责规范化路径、防止 `..` 或绝对路径逃逸、创建父目录和覆盖写入。适合单机开发，不适合多实例共享生产文件。

## 5. 主链路

存储：

```text
StoreFileCommand
  -> validate bucket/objectKey
  -> resolve normalized path under root
  -> create parent directories
  -> copy stream
  -> return FileObject
```

读取：

```text
bucket/objectKey
  -> safe path resolution
  -> file exists?
  -> metadata + opened InputStream
  -> caller closes stream
```

## 6. 安全与失败边界

- 路径必须 normalize 并验证仍位于 root 下。
- bucket/objectKey 不能接受绝对路径或路径逃逸。
- contentType 来自调用方，不等于可信文件类型。
- 当前不内置大小、扩展名、checksum 和病毒策略。
- store 覆盖同名对象；业务需要版本化时必须生成版本 objectKey。
- 多实例本地磁盘不共享，不能宣称高可用。
- `load` InputStream 必须由调用方关闭。

## 7. 扩展原则

- MinIO/S3/OSS：实现 `FileStorage` adapter，在单独模块或 Platform 文件服务中引入 SDK。
- 预签名 URL 与权限：Platform 文件服务负责。
- checksum、加密和扫描：通过 adapter 或上层服务策略实现。
- 自定义 `FileStorage` Bean 时 Local 默认实现退让。

## 8. 源码阅读顺序

```text
StoreFileCommand
  -> FileObject
  -> StoredFile
  -> FileStorage
  -> LocalFileStorage path validation
  -> SynapseFileProperties
  -> SynapseFileAutoConfiguration
  -> traversal and stream lifecycle tests
```

## 9. 手写练习

1. 保存并读取一个文本文件，正确关闭两个 InputStream。
2. 使用 `../outside.txt` 验证路径逃逸被拒绝。
3. 两次写同一 objectKey，观察覆盖语义。
4. 实现内存 FileStorage 并替换 Local Bean。

## 10. 修改检查清单

- 是否加入业务附件字段或 Controller。
- 是否把 objectKey 当公网 URL。
- 是否存在路径逃逸。
- 是否明确 InputStream 的关闭责任。
- 是否把 LocalFileStorage 描述为多实例生产存储。
- 是否引入对象存储 SDK 污染基础模块。
- 用户自定义 FileStorage 是否覆盖默认实现。
