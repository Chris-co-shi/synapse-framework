package com.indigo.synapse.file.storage;

import java.io.InputStream;

/**
 * 已读取的文件。
 *
 * <p>该对象组合文件元数据和文件内容流。调用方获取后应负责关闭 content，避免底层文件句柄或网络连接泄露。</p>
 *
 * @param metadata 文件元数据
 * @param content 文件内容流
 */
public record StoredFile(FileObject metadata, InputStream content) {

    public StoredFile {
        if (metadata == null) {
            throw new IllegalArgumentException("metadata must not be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
    }
}
