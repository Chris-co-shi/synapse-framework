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

public final class LocalFileStorage implements FileStorage {

    private final Path rootDirectory;

    public LocalFileStorage(Path rootDirectory) {
        if (rootDirectory == null) {
            throw new IllegalArgumentException("rootDirectory must not be null");
        }
        this.rootDirectory = rootDirectory.toAbsolutePath().normalize();
    }

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
