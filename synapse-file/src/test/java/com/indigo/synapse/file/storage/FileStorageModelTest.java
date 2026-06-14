package com.indigo.synapse.file.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageModelTest {

    @Test
    void shouldValidateStoreFileCommand() {
        ByteArrayInputStream content = new ByteArrayInputStream("sample".getBytes());

        StoreFileCommand command = new StoreFileCommand("bucket", "sample.txt", "text/plain", content);

        assertEquals("bucket", command.bucket());
        assertThrows(IllegalArgumentException.class,
                () -> new StoreFileCommand("", "sample.txt", "text/plain", content));
        assertThrows(IllegalArgumentException.class,
                () -> new StoreFileCommand("bucket", " ", "text/plain", content));
        assertThrows(IllegalArgumentException.class,
                () -> new StoreFileCommand("bucket", "sample.txt", "text/plain", null));
    }

    @Test
    void shouldValidateFileObject() {
        FileObject fileObject = new FileObject("bucket", "sample.txt", "text/plain", 6);

        assertEquals(6, fileObject.size());
        assertThrows(IllegalArgumentException.class,
                () -> new FileObject("", "sample.txt", "text/plain", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new FileObject("bucket", "", "text/plain", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new FileObject("bucket", "sample.txt", "text/plain", -1));
    }

    @Test
    void shouldValidateStoredFile() {
        FileObject metadata = new FileObject("bucket", "sample.txt", "text/plain", 6);
        ByteArrayInputStream content = new ByteArrayInputStream("sample".getBytes());

        StoredFile storedFile = new StoredFile(metadata, content);

        assertEquals(metadata, storedFile.metadata());
        assertThrows(IllegalArgumentException.class, () -> new StoredFile(null, content));
        assertThrows(IllegalArgumentException.class, () -> new StoredFile(metadata, null));
    }
}
