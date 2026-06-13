package com.indigo.synapse.file.storage;

import java.io.InputStream;

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
