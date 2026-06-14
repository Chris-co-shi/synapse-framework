package com.indigo.synapse.file.storage.local;

import com.indigo.synapse.file.storage.FileObject;
import com.indigo.synapse.file.storage.FileStorage;
import com.indigo.synapse.file.storage.StoreFileCommand;
import com.indigo.synapse.file.storage.StoredFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * 基于本地文件系统的 FileStorage 实现。
 *
 * <p>该实现适合开发、测试、单机部署或轻量场景。它将 bucket 映射为根目录下的一级目录，将 objectKey
 * 映射为 bucket 内的相对路径，并做基础路径穿越防护，确保最终路径不逃逸 rootDirectory。</p>
 *
 * <p>该实现不提供分布式共享、权限控制、访问 URL、预签名 URL、文件版本、转码、预览或对象存储能力。
 * 生产系统如需要这些能力，应提供自定义 {@link FileStorage} 实现。</p>
 */
public final class LocalFileStorage implements FileStorage {

    private final Path rootDirectory;

    public LocalFileStorage(Path rootDirectory) {
        if (rootDirectory == null) {
            throw new IllegalArgumentException("rootDirectory must not be null");
        }
        this.rootDirectory = rootDirectory.toAbsolutePath().normalize();
    }

    /**
     * 保存文件到本地文件系统。
     *
     * <p>如果目标文件已存在，会覆盖写入。</p>
     */
    @Override
    public FileObject store(StoreFileCommand command) {
        Path target = resolve(command.bucket(), command.objectKey());
        try {
            Files.createDirectories(target.getParent());
            Files.copy(command.content(), target, StandardCopyOption.REPLACE_EXISTING);
            return new FileObject(command.bucket(), command.objectKey(), command.contentType(), Files.size(target));
        } catch (IOException exception) {
            throw new UncheckedIOException("failed to store file", exception);
        }
    }

    /**
     * 从本地文件系统读取文件。
     *
     * <p>返回的 InputStream 需要由调用方关闭。</p>
     */
    @Override
    public Optional<StoredFile> load(String bucket, String objectKey) {
        Path source = resolve(bucket, objectKey);
        if (!Files.isRegularFile(source)) {
            return Optional.empty();
        }
        try {
            InputStream content = Files.newInputStream(source);
            FileObject metadata = new FileObject(bucket, objectKey, null, Files.size(source));
            return Optional.of(new StoredFile(metadata, content));
        } catch (IOException exception) {
            throw new UncheckedIOException("failed to load file", exception);
        }
    }

    /**
     * 删除本地文件。
     */
    @Override
    public boolean delete(String bucket, String objectKey) {
        try {
            return Files.deleteIfExists(resolve(bucket, objectKey));
        } catch (IOException exception) {
            throw new UncheckedIOException("failed to delete file", exception);
        }
    }

    private Path resolve(String bucket, String objectKey) {
        validateSegment(bucket, "bucket");
        validateObjectKey(objectKey);
        Path resolved = rootDirectory.resolve(bucket).resolve(objectKey).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("objectKey must stay inside rootDirectory");
        }
        return resolved;
    }

    private static void validateSegment(String value, String name) {
        if (value == null || value.isBlank() || value.contains("..") || value.contains("/") || value.contains("\\")) {
            throw new IllegalArgumentException(name + " is invalid");
        }
    }

    private static void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/") || objectKey.startsWith("\\")
                || objectKey.contains("\\")) {
            throw new IllegalArgumentException("objectKey is invalid");
        }
        Path objectPath = Path.of(objectKey);
        if (objectPath.isAbsolute()) {
            throw new IllegalArgumentException("objectKey is invalid");
        }
        for (Path segment : objectPath) {
            if ("..".equals(segment.toString())) {
                throw new IllegalArgumentException("objectKey is invalid");
            }
        }
    }
}
