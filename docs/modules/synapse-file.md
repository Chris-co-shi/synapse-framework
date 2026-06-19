# synapse-file 使用手册

## 1. 模块定位

`synapse-file` 是 Synapse Framework 的文件存储抽象模块。

它提供最小文件存储 SPI、文件保存命令、文件元数据模型、读取结果模型、本地文件系统实现和自动配置。

当前核心能力：

- `FileStorage` 文件存储 SPI。
- `StoreFileCommand` 保存文件命令。
- `FileObject` 文件对象元数据。
- `StoredFile` 已读取文件结果。
- `LocalFileStorage` 本地文件系统实现。
- `SynapseFileAutoConfiguration` 自动配置。
- `SynapseFileProperties` 本地存储根目录配置。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-file`：

- 需要统一文件存储接口。
- 需要在业务服务中按 bucket / objectKey 保存和读取文件。
- 需要开发或测试环境的本地文件存储实现。
- 需要为后续 MinIO、OSS、S3、私有文件服务等实现提供统一 SPI。
- 需要避免业务系统直接绑定具体文件存储实现。

## 3. 不适用场景

`synapse-file` 不适合承担以下职责：

- 上传 Controller。
- 下载 Controller。
- 文件中心后台。
- 附件表。
- 文件权限。
- 文件访问 URL。
- 预签名 URL。
- 文件预览。
- 转码。
- OCR。
- 水印。
- CDN。
- 对象存储完整 SDK 封装。

这些能力应由业务系统、平台文件服务或后续 adapter 模块实现。

## 4. Maven 引入

推荐先引入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

再引入 file 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-file</artifactId>
</dependency>
```

## 5. 核心能力

### 5.1 FileStorage

核心接口：

```java
FileStorage
```

方法：

```java
FileObject store(StoreFileCommand command);
Optional<StoredFile> load(String bucket, String objectKey);
boolean delete(String bucket, String objectKey);
```

说明：

- `store` 保存文件并返回元数据。
- `load` 返回文件元数据和内容流。
- `delete` 删除文件。
- `load` 返回的 `InputStream` 由调用方负责关闭。

### 5.2 StoreFileCommand

字段：

```text
bucket
objectKey
contentType
content
```

说明：

- `bucket` 是存储桶或逻辑命名空间。
- `objectKey` 是对象 key。
- `contentType` 可为空。
- `content` 是文件内容流。

该模型不包含上传人、附件 ID、业务单据、文件权限等业务信息。

### 5.3 FileObject

字段：

```text
bucket
objectKey
contentType
size
```

它只描述文件存储层元数据，不描述业务附件。

### 5.4 StoredFile

字段：

```text
metadata
content
```

注意：`content` 是 `InputStream`，调用方读取后必须关闭。

### 5.5 LocalFileStorage

默认本地实现：

```text
rootDirectory / bucket / objectKey
```

特性：

- 自动创建父目录。
- 同名文件覆盖写入。
- 文件不存在时 `load` 返回 empty。
- 删除不存在文件时返回 false。
- 对 bucket 和 objectKey 做基础路径校验。
- 防止 objectKey 逃逸 rootDirectory。

适合：

- 开发环境。
- 测试环境。
- 单机轻量部署。

不适合：

- 多实例共享文件。
- 对象存储。
- 权限控制。
- 文件分发。
- 大规模文件服务。

## 6. 快速使用

### 6.1 保存文件

```java
try (InputStream input = Files.newInputStream(Path.of("sample.txt"))) {
    FileObject object = fileStorage.store(new StoreFileCommand(
            "sample",
            "docs/sample.txt",
            "text/plain",
            input
    ));
}
```

### 6.2 读取文件

```java
Optional<StoredFile> loaded = fileStorage.load("sample", "docs/sample.txt");

if (loaded.isPresent()) {
    try (InputStream content = loaded.get().content()) {
        byte[] bytes = content.readAllBytes();
    }
}
```

### 6.3 删除文件

```java
boolean deleted = fileStorage.delete("sample", "docs/sample.txt");
```

### 6.4 配置本地根目录

```yaml
synapse:
  file:
    local-root: /data/synapse-file
```

## 7. 扩展方式

### 7.1 替换 FileStorage

业务系统或平台文件服务可以提供自定义 Bean：

```java
@Bean
FileStorage fileStorage() {
    return new MinioFileStorage(...);
}
```

提供自定义 `FileStorage` 后，默认 `LocalFileStorage` 不会覆盖。

### 7.2 对接对象存储

对象存储实现应放在业务系统、平台服务或后续 adapter 模块中，例如：

```text
platform-file-service
synapse-file-minio-adapter
synapse-file-s3-adapter
```

当前不在 `synapse-file` 主模块中直接引入对象存储 SDK。

## 8. 配置项

配置前缀：

```yaml
synapse.file
```

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `local-root` | `${java.io.tmpdir}/synapse-file` | 本地文件存储根目录 |

## 9. 边界与注意事项

### 9.1 file 不等于文件中心

`synapse-file` 是存储抽象，不是平台文件中心。

文件中心通常还需要：

- 文件元数据表。
- 权限模型。
- 上传下载 API。
- 访问 URL。
- 文件生命周期。
- 文件预览和转码。

这些不属于 framework 文件存储抽象。

### 9.2 不要把业务附件模型放入 synapse-file

例如：

- attachmentId
- businessType
- businessId
- uploaderId
- filePurpose

这些属于业务系统或平台文件服务。

### 9.3 LocalFileStorage 不适合多实例共享

多实例服务如果都使用本地磁盘，文件不会自动同步。生产系统通常应使用对象存储、共享文件系统或平台文件服务。

### 9.4 objectKey 是存储 key，不是外部访问路径

不要直接把 objectKey 当作公网访问 URL。访问控制和 URL 生成应由业务系统或平台文件服务处理。

### 9.5 当前没有大小和类型策略

当前不内置文件大小限制、contentType 白名单或 checksum 校验。需要这些策略时，应在业务系统或文件平台能力中处理。

## 10. 常见问题

### Q1：为什么没有上传/下载 Controller？

因为上传下载接口属于业务系统或平台文件服务。framework 只提供存储抽象。

### Q2：为什么没有附件表？

附件表强依赖业务关系，例如单据、用户、流程、权限和生命周期，不适合作为 framework 默认表。

### Q3：可以用 MinIO / OSS / S3 吗？

可以，但应通过自定义 `FileStorage` 实现接入。主模块当前不直接引入对象存储 SDK。

### Q4：LocalFileStorage 是否安全？

它包含基础路径校验和路径逃逸防护，但不提供完整文件安全策略。生产系统仍应根据部署环境增加权限、大小、类型、病毒扫描等外部控制。

### Q5：业务系统应该保存什么？

业务系统通常保存自己的业务附件记录，例如业务 ID、文件用途、bucket、objectKey、展示名、创建人等。`synapse-file` 只负责存储和读取文件内容。

## 11. Configuration Metadata

`synapse-file` 发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，覆盖 `synapse.file.local-root`。新增本地存储配置时必须说明路径格式、默认值和边界。
