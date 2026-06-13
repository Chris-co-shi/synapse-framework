package com.indigo.synapse.file.storage;

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
