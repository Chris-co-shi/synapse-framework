package com.indigo.synapse.file.storage;

import java.util.Optional;

/**
 * 文件存储 SPI。
 *
 * <p>该接口只抽象最小文件存储能力：保存、读取和删除。framework 不关心文件来自 HTTP 上传、
 * MQ 消息、任务生成还是业务系统内部产生，也不定义附件表、文件权限、预览、转码、OCR 或水印。</p>
 *
 * <p>业务系统或平台文件服务可以实现该接口，对接本地文件系统、对象存储、私有文件服务或其他存储设施。</p>
 */
public interface FileStorage {

    /**
     * 保存文件内容。
     *
     * @param command 保存命令
     * @return 保存后的文件对象元数据
     */
    FileObject store(StoreFileCommand command);

    /**
     * 读取文件。
     *
     * <p>返回的 InputStream 由调用方负责关闭。</p>
     *
     * @param bucket 存储桶或逻辑命名空间
     * @param objectKey 对象 key
     * @return 文件内容和元数据；不存在时返回 empty
     */
    Optional<StoredFile> load(String bucket, String objectKey);

    /**
     * 删除文件。
     *
     * @param bucket 存储桶或逻辑命名空间
     * @param objectKey 对象 key
     * @return 成功删除或目标已存在并被删除时返回 true；不存在时返回 false
     */
    boolean delete(String bucket, String objectKey);
}
