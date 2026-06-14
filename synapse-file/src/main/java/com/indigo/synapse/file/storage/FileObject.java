package com.indigo.synapse.file.storage;

/**
 * 文件对象元数据。
 *
 * <p>该模型只描述文件存储层的基础元数据，不表达业务附件、上传人、权限、访问 URL、预览状态或转码结果。</p>
 *
 * @param bucket 存储桶或逻辑命名空间
 * @param objectKey 对象 key
 * @param contentType 内容类型；可能为空，取决于具体存储实现和调用方输入
 * @param size 文件大小，单位字节
 */
public record FileObject(
        String bucket,
        String objectKey,
        String contentType,
        long size
) {

    public FileObject {
        validate(bucket, "bucket");
        validate(objectKey, "objectKey");
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
    }

    private static void validate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
