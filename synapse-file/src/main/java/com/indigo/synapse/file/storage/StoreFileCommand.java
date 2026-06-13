package com.indigo.synapse.file.storage;

import java.io.InputStream;

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
