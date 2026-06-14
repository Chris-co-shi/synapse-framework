package com.indigo.synapse.file.storage;

import java.io.InputStream;

/**
 * 保存文件命令。
 *
 * <p>该命令只表达写入文件存储所需的最小输入：bucket、objectKey、contentType 和内容流。
 * 文件名、上传人、业务附件 ID、访问权限、文件来源等业务信息不属于该模型。</p>
 *
 * @param bucket 存储桶或逻辑命名空间，不能为空
 * @param objectKey 对象 key，不能为空
 * @param contentType 内容类型；允许为空，由调用方决定是否维护
 * @param content 文件内容流，不能为空；调用方负责提供可读取流
 */
public record StoreFileCommand(
        String bucket,
        String objectKey,
        String contentType,
        InputStream content
) {

    public StoreFileCommand {
        validate(bucket, "bucket");
        validate(objectKey, "objectKey");
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
    }

    private static void validate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
